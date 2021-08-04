package com.test.taskmanager.app.view

import com.test.taskmanager.domain.SortingType.*
import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.ListResult
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import io.vavr.control.Try
import java.util.function.Consumer

class ListProcessScreen(taskManager: TaskManager) : AbstractScreen<Void?>(taskManager) {
    override fun invoke(): Void? {
        promptBuilder.createListPrompt()
                .name(ACTION)
                .message("Process list sort type:")
                .newItem(CREATION_TIME_ACTION).text("1 - Creation time").add()
                .newItem(PRIORITY_ACTION).text("2 - Priority").add()
                .newItem(ID_ACTION).text("3 - Id").add()
                .newItem(BACK_ACTION).text("4 - Back").add()
                .addPrompt()
        val result: Map<String, PromtResultItemIF> = Try.of { prompt.prompt(promptBuilder.build()) }
                .get()

        val action = result[ACTION] as ListResult

        when (action.selectedId) {
            CREATION_TIME_ACTION -> taskManager.listAll(CREATION_TIME)
                    .forEach(Consumer(::println))
            PRIORITY_ACTION -> taskManager.listAll(PRIORITY)
                    .forEach(Consumer(::println))
            ID_ACTION -> taskManager.listAll(ID)
                    .forEach(Consumer(::println))
            BACK_ACTION -> MainScreen(taskManager).invoke()
        }

        ListProcessScreen(taskManager).invoke()
        return null
    }

    companion object {
        private const val CREATION_TIME_ACTION = "creationTime"
        private const val PRIORITY_ACTION = "priority"
        private const val ID_ACTION = "id"
    }
}