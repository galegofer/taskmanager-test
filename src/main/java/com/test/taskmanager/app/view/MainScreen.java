package com.test.taskmanager.app.view;

import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import io.vavr.control.Try;
import java.util.Map;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class MainScreen extends AbstractScreen<Void> {

    private static final int SUCCESS = 0;
    private static final String ADD_ACTION = "add";
    private static final String LIST_ACTION = "list";
    private static final String KILL_ACTION = "kill";
    private static final String EXIT_ACTION = "exit";

    public Void invoke() {
        promptBuilder.createListPrompt()
            .name(ACTION)
            .message("Please choose an action to perform")
            .newItem(ADD_ACTION).text("1 - Add Process").add()
            .newItem(LIST_ACTION).text("2 - List Process").add()
            .newItem(KILL_ACTION).text("3 - Kill a Process").add()
            .newItem(EXIT_ACTION).text("4 - Exit Application").add()
            .addPrompt();

        Map<String, ? extends PromtResultItemIF> result = Try.of(() -> prompt.prompt(promptBuilder.build()))
            .get();

        ListResult action = (ListResult) result.get(ACTION);

        switch (action.getSelectedId()) {
            case ADD_ACTION:
                AddProcessScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
                break;
            case LIST_ACTION:
                ListProcessScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
                break;
            case KILL_ACTION:
                KillProcessScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
                break;
            case EXIT_ACTION:
                System.exit(SUCCESS);
        }

        return null;
    }
}
