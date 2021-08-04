package com.test.taskmanager.service.impl

import com.test.taskmanager.domain.Priority
import com.test.taskmanager.domain.Process
import com.test.taskmanager.domain.SortingType
import com.test.taskmanager.domain.SortingType.*
import com.test.taskmanager.service.TaskManager
import io.vavr.concurrent.Future
import io.vavr.control.Option
import mu.KotlinLogging
import java.util.*
import java.util.Collections.unmodifiableList
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors.toUnmodifiableList

class TaskManagerImpl : TaskManager {

    private val log = KotlinLogging.logger {}

    private val runningProcesses: MutableList<Process> = CopyOnWriteArrayList()

    private val logWhenMaxProcessesReached = Function { process: Process ->
        Option.none<Process>()
                .onEmpty { log.error("Too many processes running already, process with PID: {} was rejected", process.pid) }
    }

    private val findOldestProcessToKill = Function { _: Process ->
        Option.some(runningProcesses[0])
    }

    private val findLowPriorityToKill = Function { _: Process ->
        Option
                .some(runningProcesses.stream()
                        .sorted(Comparator.comparing(Process::priority)
                                .thenComparing(Process::creationTime))
                        .reduce { _: Process, second: Process -> second }
                        .orElse(null))
    }

    private val logKillFunction = Function { process: Process ->
        Consumer { processToKill: Process ->
            log.info(
                    "Killing process PID: {}, priority: {}, date: {}, in favor of process PID: {}, priority: {}, date: {}",
                    processToKill.pid, processToKill.priority, processToKill.creationTime,
                    process.pid, process.priority, process.creationTime)
        }
    }

    override fun add(priority: Priority): Optional<Process> {
        return validateProcess(Process(priority = priority), logWhenMaxProcessesReached)
    }

    override fun addToFifo(priority: Priority): Optional<Process> {
        return validateProcess(Process(priority = priority), findOldestProcessToKill)
    }

    // FIXME: Need to order by time also
    override fun addWithPriority(priority: Priority): Optional<Process> {
        return validateProcess(Process(priority = priority), findLowPriorityToKill)
    }

    override fun listAll(sortingType: SortingType?): List<Process> {
        return when (sortingType) {
            ID -> runningProcesses.stream()
                    .sorted(Comparator.comparing(Process::pid))
                    .collect(toUnmodifiableList())
            PRIORITY -> runningProcesses.stream()
                    .sorted(Comparator.comparing(Process::priority))
                    .collect(toUnmodifiableList())
            CREATION_TIME -> runningProcesses.stream()
                    .sorted(Comparator.comparing(Process::creationTime))
                    .collect(toUnmodifiableList())
            else -> unmodifiableList(runningProcesses)
        }
    }

    override fun kill(process: Process?): Optional<Process?> {
        return Option.of(process)
                .peek { value -> kill(value!!.pid) }
                .onEmpty { log.error("Provided process has a PID that is null or empty") }
                .toJavaOptional()
    }

    override fun kill(pid: Long): Optional<Process> {
        return Option.ofOptional(runningProcesses.stream()
                .filter { process -> process.pid == pid }
                .findAny())
                .onEmpty { log.warn("Process with PID: {}, not found to kill", pid) }
                .map { processToKill ->
                    runningProcesses.removeIf { `object`: Process -> `object`.pid == pid }
                    processToKill.kill()
                }.toJavaOptional()
    }

    override fun kill(priority: Priority): List<Process> {
        return runningProcesses.stream()
                .filter { process -> process.priority == priority }
                .map { processToKill ->
                    runningProcesses.removeIf { `object` -> priority == `object`.priority }
                    processToKill.kill()
                }.collect(toUnmodifiableList())
    }

    override fun killAll(): List<Process> {
        val killedProcesses = runningProcesses.stream()
                .map(Process::kill)
                .collect(toUnmodifiableList())
        runningProcesses.clear()
        return killedProcesses
    }

    private fun validateProcess(process: Process, actionOnMaxCapacityReached: Function<Process, Option<Process>>): Optional<Process> {
        return checkAndAddProcess(process, actionOnMaxCapacityReached)
                .toJavaOptional()
    }

    private fun checkAndAddProcess(process: Process, actionOnMaxCapacityReached: Function<Process, Option<Process>>): Option<Process> {
        return Option.of(process)
                .filter { runningProcesses.size < MAX_PROCESSES }
                .map { computation -> Future.runRunnable(computation) }
                .map(addFutureToProcessAndProcessToList(process))
                .peek { result -> log.info("Running process PID: {}, priority: {}", result.pid, result.priority) }
                .orElse { executeOverCapacityAction(process, actionOnMaxCapacityReached) }
    }

    private fun executeOverCapacityAction(process: Process, function: Function<Process, Option<Process>>): Option<Process> {
        return Option.of(process)
                .flatMap(function)
                .peek { processToKill ->
                    logKillFunction.apply(process)
                            .accept(processToKill)
                }
                .map(this::kill)
                .map { Future.runRunnable(process) }
                .map(addFutureToProcessAndProcessToList(process))
    }

    private fun addFutureToProcessAndProcessToList(process: Process): Function<Future<Void>, Process> {
        return Function { future ->
            val processWithFuture = process.copy(future = future)
            runningProcesses.add(processWithFuture)
            processWithFuture
        }
    }

    companion object {
        const val MAX_PROCESSES = 4
    }
}