package com.test.taskmanager.app.view

import com.test.taskmanager.domain.Priority
import com.test.taskmanager.domain.Priority.*
import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.ListResult
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import io.vavr.control.Try

class ProcessPriorityScreen(taskManager: TaskManager) : AbstractScreen<Priority>(taskManager) {
    override fun invoke(): Priority {
        promptBuilder.createListPrompt()
                .name(ACTION)
                .message("Add process priority:")
                .newItem(LOW_ACTION).text("1 - Low").add()
                .newItem(MEDIUM_ACTION).text("2 - Medium").add()
                .newItem(HIGH_ACTION).text("3 - High").add()
                .newItem(BACK_ACTION).text("4 - Back").add()
                .addPrompt()

        val result: Map<String, PromtResultItemIF> = Try.of { prompt.prompt(promptBuilder.build()) }
                .get()
        val action = result[ACTION] as ListResult

        if (BACK_ACTION == action.selectedId) {
            AddProcessScreen(taskManager).invoke()
        }

        return when (action.selectedId) {
            LOW_ACTION -> LOW
            MEDIUM_ACTION -> MEDIUM
            HIGH_ACTION -> HIGH
            else -> LOW
        }
    }

    companion object {
        private const val LOW_ACTION = "low"
        private const val MEDIUM_ACTION = "medium"
        private const val HIGH_ACTION = "high"
    }
}