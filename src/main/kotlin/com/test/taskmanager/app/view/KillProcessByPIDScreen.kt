package com.test.taskmanager.app.view

import com.test.taskmanager.domain.Process
import com.test.taskmanager.domain.SortingType
import com.test.taskmanager.domain.SortingType.ID
import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.CheckboxResult
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import io.vavr.control.Option
import io.vavr.control.Try
import java.util.*
import java.util.function.Consumer

class KillProcessByPIDScreen(taskManager: TaskManager) : AbstractScreen<Void?>(taskManager) {
    override fun invoke(): Void? {
        val processes = taskManager.listAll(ID)

        if (processes.isNotEmpty()) {
            val builder = promptBuilder.createCheckboxPrompt()
                    .name(ACTION)
                    .message("Kill Processes by PID:")
            processes
                    .forEach(Consumer { process ->
                        builder.newItem(process.pid.toString())
                                .text("Process PID: " + process.pid + " priority: " + process.priority)
                                .add()
                    })
            builder.addPrompt()

            val result: Map<String, PromtResultItemIF> = Try.of { prompt.prompt(promptBuilder.build()) }
                    .get()
            val action = result[ACTION] as CheckboxResult

            if (action.selectedIds.isEmpty()) {
                println("No process selected to kill")
                KillProcessScreen(taskManager).invoke()
            }

            action.selectedIds.stream()
                    .map { pidsToKill -> taskManager.kill(pidsToKill.toLong()) }
                    .forEach { killedProcess ->
                        Option.ofOptional(killedProcess)
                                .onEmpty { println("No process to kill with given priority") }
                                .peek { process: Process? -> println(process.toString() + " killed") }
                    }
        } else {
            println("There is no processes to kill")
        }

        KillProcessScreen(taskManager).invoke()
        return null
    }
}