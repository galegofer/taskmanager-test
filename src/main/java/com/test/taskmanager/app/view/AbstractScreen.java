package com.test.taskmanager.app.view;

import com.test.taskmanager.service.TaskManager;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AbstractScreen<T> {

    protected final ConsolePrompt prompt = new ConsolePrompt();
    protected final PromptBuilder promptBuilder = new ConsolePrompt().getPromptBuilder();

    protected static final String ACTION = "action";
    protected static final String BACK_ACTION = "back";
    protected final TaskManager taskManager;

    protected AbstractScreen(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public abstract T invoke();
}
