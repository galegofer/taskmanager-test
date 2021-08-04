package com.test.taskmanager.app.view

import com.test.taskmanager.domain.Process
import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.ListResult
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import io.vavr.control.Try
import org.fusesource.jansi.Ansi

class AddProcessScreen(taskManager: TaskManager) : AbstractScreen<Void?>(taskManager) {
    override fun invoke(): Void? {
        promptBuilder.createListPrompt()
                .name(ACTION)
                .message("Add process version:")
                .newItem(REGULAR_ACTION).text("1 - Regular Process").add()
                .newItem(FIFO_ACTION).text("2 - FIFO Process").add()
                .newItem(PRIORITY_ACTION).text("3 - Process with Priority").add()
                .newItem(BACK_ACTION).text("4 - Back").add()
                .addPrompt()

        val result: Map<String, PromtResultItemIF> = Try.of { prompt.prompt(promptBuilder.build()) }
                .get()
        val action = result[ACTION] as ListResult

        if (BACK_ACTION == action.selectedId) {
            MainScreen(taskManager).invoke()
        }

        val priority = ProcessPriorityScreen(taskManager).invoke()

        when (action.selectedId) {
            REGULAR_ACTION -> taskManager.add(priority)
                    .ifPresentOrElse({ process ->
                        println(Ansi.ansi().render(String.format("Created process with PID: %d and priority: %s", process.pid,
                                process.priority)))
                    }) {
                        println(Ansi.ansi()
                                .render("Process couldn't be created, because the task manager is at limit of processes"))
                    }
            FIFO_ACTION -> taskManager.addToFifo(priority)
                    .ifPresent { process ->
                        println(Ansi.ansi().render(String.format("Created process with PID: %d and priority: %s for FIFO", process.pid,
                                process.priority)))
                    }
            PRIORITY_ACTION -> taskManager.addWithPriority(priority)
                    .ifPresent { process ->
                        println(Ansi.ansi().render(String.format("Created process with PID: %d and priority: %s killing by priority",
                                process.pid, process.priority)))
                    }
            BACK_ACTION -> MainScreen(taskManager).invoke()
        }

        AddProcessScreen(taskManager).invoke()
        return null
    }

    companion object {
        private const val REGULAR_ACTION = "regular"
        private const val FIFO_ACTION = "fifo"
        private const val PRIORITY_ACTION = "priority"
    }
}