package com.test.taskmanager.app.view

import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.builder.PromptBuilder

abstract class AbstractScreen<T> protected constructor(protected val taskManager: TaskManager) {
    protected val prompt = ConsolePrompt()
    protected val promptBuilder: PromptBuilder = prompt.promptBuilder
    abstract operator fun invoke(): T

    companion object {
        const val ACTION = "action"
        const val BACK_ACTION = "back"
    }
}