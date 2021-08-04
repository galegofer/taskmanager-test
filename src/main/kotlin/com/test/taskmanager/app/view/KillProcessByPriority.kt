package com.test.taskmanager.app.view

import com.test.taskmanager.domain.Priority
import com.test.taskmanager.domain.Priority.*
import com.test.taskmanager.domain.Process
import com.test.taskmanager.domain.SortingType.ID
import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.CheckboxResult
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import io.vavr.control.Try
import java.util.function.Consumer

class KillProcessByPriority(taskManager: TaskManager) : AbstractScreen<Void?>(taskManager) {
    override fun invoke(): Void? {
        promptBuilder.createCheckboxPrompt()
                .name(ACTION)
                .message("Kill by Priority:")
                .newItem(LOW_ACTION).text("Low").add()
                .newItem(MEDIUM_ACTION).text("Medium").add()
                .newItem(HIGH_ACTION).text("High").add()
                .addPrompt()
        val result: Map<String, PromtResultItemIF> = Try.of { prompt.prompt(promptBuilder.build()) }
                .get()
        val action = result[ACTION] as CheckboxResult

        if (action.selectedIds.isEmpty()) {
            println("No process selected to kill")
            KillProcessScreen(taskManager).invoke()
        }

        action.selectedIds
                .forEach(Consumer { selectedId ->
                    when (selectedId) {
                        LOW_ACTION -> killTaskByPriority(LOW)
                        MEDIUM_ACTION -> killTaskByPriority(MEDIUM)
                        HIGH_ACTION -> killTaskByPriority(HIGH)
                    }
                })
        KillProcessScreen(taskManager).invoke()
        return null
    }

    private fun killTaskByPriority(priority: Priority) {
        taskManager.listAll(ID).stream()
                .map(Process::priority)
                .filter { currentPriority -> currentPriority == priority }
                .forEach(taskManager::kill)
    }

    companion object {
        private const val LOW_ACTION = "low"
        private const val MEDIUM_ACTION = "medium"
        private const val HIGH_ACTION = "high"
    }
}