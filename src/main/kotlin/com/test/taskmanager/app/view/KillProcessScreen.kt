package com.test.taskmanager.app.view

import com.test.taskmanager.service.TaskManager
import de.codeshelf.consoleui.prompt.ListResult
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import io.vavr.control.Try
import java.util.function.Consumer

class KillProcessScreen(taskManager: TaskManager) : AbstractScreen<Void?>(taskManager) {
    override fun invoke(): Void? {
        promptBuilder.createListPrompt()
                .name(ACTION)
                .message("Kill Processes:")
                .newItem(PID_ACTION).text("1 - Kill process by PID").add()
                .newItem(PRIORITY_ACTION).text("2 - Kill processes by priority").add()
                .newItem(ALL_ACTION).text("3 - Kill all the processes").add()
                .newItem(BACK_ACTION).text("4 - Back").add()
                .addPrompt()

        val result: Map<String, PromtResultItemIF> = Try.of { prompt.prompt(promptBuilder.build()) }
                .get()
        val action = result[ACTION] as ListResult

        when (action.selectedId) {
            PID_ACTION -> KillProcessByPIDScreen(taskManager).invoke()
            PRIORITY_ACTION -> KillProcessByPriority(taskManager).invoke()
            ALL_ACTION -> {
                println("ALl processes killed: ")
                taskManager.killAll().forEach(Consumer { killedProcess -> println("$killedProcess killed") })
            }
            BACK_ACTION -> MainScreen(taskManager).invoke()
        }

        KillProcessScreen(taskManager).invoke()
        return null
    }

    companion object {
        private const val PID_ACTION = "pid"
        private const val PRIORITY_ACTION = "priority"
        private const val ALL_ACTION = "all"
    }
}