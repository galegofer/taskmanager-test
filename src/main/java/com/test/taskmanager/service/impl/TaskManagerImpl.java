package com.test.taskmanager.service.impl;

import static com.test.taskmanager.domain.SortingType.CREATION_TIME;
import static com.test.taskmanager.domain.SortingType.ID;
import static com.test.taskmanager.domain.SortingType.PRIORITY;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.test.taskmanager.domain.Priority;
import com.test.taskmanager.domain.Process;
import com.test.taskmanager.domain.SortingType;
import com.test.taskmanager.service.TaskManager;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TaskManagerImpl implements TaskManager {

    public static final int MAX_PROCESSES = 4;

    private final List<Process> runningProcesses = new CopyOnWriteArrayList<>();

    private final Function<Process, Option<Process>> logWhenMaxProcessesReached = process -> Option.<Process>none()
        .onEmpty(
            () -> log.error("Too many processes running already, process with PID: {} was rejected", process.getPid()));
    private final Function<Process, Option<Process>> findOldestProcessToKill = process -> Option
        .some(runningProcesses.get(0));
    private final Function<Process, Option<Process>> findLowPriorityToKill = process -> Option
        .some(runningProcesses.stream()
            .sorted(Comparator.comparing(Process::getPriority)
                .thenComparing(Process::getCreationTime))
            .reduce((first, second) -> second)
            .orElse(null));

    private final Function<Process, Consumer<Process>> logKillFunction =
        process -> processToKill -> log.info(
            "Killing process PID: {}, priority: {}, date: {}, in favor of process PID: {}, priority: {}, date: {}",
            processToKill.getPid(), processToKill.getPriority(), processToKill.getCreationTime(),
            process.getPid(), process.getPriority(), process.getCreationTime());

    @Override
    public Optional<Process> add(@NonNull Priority priority) {
        return validateProcess(Process.builder()
            .priority(priority)
            .build(), logWhenMaxProcessesReached);
    }

    @Override
    public Optional<Process> addToFifo(@NonNull Priority priority) {
        return validateProcess(Process.builder()
            .priority(priority)
            .build(), findOldestProcessToKill);
    }

    // FIXME: Need to order by time also
    @Override
    public Optional<Process> addWithPriority(@NonNull Priority priority) {
        return validateProcess(Process.builder()
            .priority(priority)
            .build(), findLowPriorityToKill);
    }

    @Override
    public List<Process> listAll(SortingType sortingType) {
        return Match(sortingType).of(
            Case($(ID), runningProcesses.stream()
                .sorted(Comparator.comparing(Process::getPid))
                .collect(toUnmodifiableList())),
            Case($(PRIORITY), runningProcesses.stream()
                .sorted(Comparator.comparing(Process::getPriority))
                .collect(toUnmodifiableList())),
            Case($(CREATION_TIME), runningProcesses.stream()
                .sorted(Comparator.comparing(Process::getCreationTime))
                .collect(toUnmodifiableList())),
            Case($(), unmodifiableList(runningProcesses)));
    }

    @Override
    public Optional<Process> kill(Process process) {
        return Option.of(process)
            .peek(value -> kill(value.getPid()))
            .onEmpty(() -> log.error("Provided process has a PID that is null or empty"))
            .toJavaOptional();
    }

    @Override
    public Optional<Process> kill(long pid) {
        return Option.ofOptional(runningProcesses.stream()
            .filter(process -> process.getPid() == pid)
            .findAny())
            .onEmpty(() -> log.warn("Process with PID: {}, not found to kill", pid))
            .map(processToKill -> {
                    runningProcesses.removeIf(object -> object.getPid() == pid);
                    return processToKill.kill();
                }
            ).toJavaOptional();
    }

    @Override
    public List<Process> kill(Priority priority) {
        return runningProcesses.stream()
            .filter(process -> process.getPriority() == priority)
            .map(processToKill -> {
                runningProcesses.removeIf(object -> priority == object.getPriority());
                return processToKill.kill();
            }).collect(toUnmodifiableList());
    }

    @Override
    public List<Process> killAll() {
        List<Process> killedProcesses = runningProcesses.stream()
            .map(Process::kill)
            .collect(toUnmodifiableList());
        runningProcesses.clear();

        return killedProcesses;
    }

    private Optional<Process> validateProcess(Process process,
        Function<Process, Option<Process>> actionOnMaxCapacityReached) {
        return checkAndAddProcess(process, actionOnMaxCapacityReached)
            .toJavaOptional();
    }

    private Option<Process> checkAndAddProcess(Process process,
        Function<Process, Option<Process>> actionOnMaxCapacityReached) {
        return Option.of(process)
            .filter(result -> runningProcesses.size() < MAX_PROCESSES)
            .map(Future::runRunnable)
            .map(addFutureToProcessAndProcessToList(process))
            .peek(result -> log.info("Running process PID: {}, priority: {}", result.getPid(), result.getPriority()))
            .orElse(() -> executeOverCapacityAction(process, actionOnMaxCapacityReached));
    }

    private Option<Process> executeOverCapacityAction(Process process, Function<Process, Option<Process>> function) {
        return Option.of(process)
            .flatMap(function)
            .peek(processToKill -> logKillFunction.apply(process).accept(processToKill))
            .map(this::kill)
            .map(processToKill -> Future.runRunnable(process))
            .map(addFutureToProcessAndProcessToList(process));
    }

    private Function<Future<Void>, Process> addFutureToProcessAndProcessToList(Process process) {
        return future -> {
            Process processWithFuture = process.withFuture(future);
            runningProcesses.add(processWithFuture);
            return processWithFuture;
        };
    }
}
