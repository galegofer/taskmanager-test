package com.test.taskmanager.app.view

import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.ListResult
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import io.vavr.control.Try
import kotlin.system.exitProcess

class MainScreen(taskManager: TaskManager) : AbstractScreen<Void?>(taskManager) {

    override fun invoke(): Void? {
        promptBuilder.createListPrompt()
                .name(ACTION)
                .message("Please choose an action to perform")
                .newItem(ADD_ACTION).text("1 - Add Process").add()
                .newItem(LIST_ACTION).text("2 - List Process").add()
                .newItem(KILL_ACTION).text("3 - Kill a Process").add()
                .newItem(EXIT_ACTION).text("4 - Exit Application").add()
                .addPrompt()

        val result: Map<String, PromtResultItemIF> = Try.of { prompt.prompt(promptBuilder.build()) }
                .get()
        val action = result[ACTION] as ListResult

        when (action.selectedId) {
            ADD_ACTION -> AddProcessScreen(taskManager).invoke()
            LIST_ACTION -> ListProcessScreen(taskManager).invoke()
            KILL_ACTION -> KillProcessScreen(taskManager).invoke()
            EXIT_ACTION -> exitProcess(SUCCESS)
        }

        return null
    }

    companion object {
        private const val SUCCESS = 0
        private const val ADD_ACTION = "add"
        private const val LIST_ACTION = "list"
        private const val KILL_ACTION = "kill"
        private const val EXIT_ACTION = "exit"
    }
}