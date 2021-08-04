package com.test.taskmanager.app.view;

import static com.test.taskmanager.domain.SortingType.CREATION_TIME;
import static com.test.taskmanager.domain.SortingType.ID;
import static com.test.taskmanager.domain.SortingType.PRIORITY;

import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import io.vavr.control.Try;
import java.util.Map;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class ListProcessScreen extends AbstractScreen<Void> {

    private static final String CREATION_TIME_ACTION = "creationTime";
    private static final String PRIORITY_ACTION = "priority";
    private static final String ID_ACTION = "id";

    @Override
    public Void invoke() {
        promptBuilder.createListPrompt()
            .name(ACTION)
            .message("Process list sort type:")
            .newItem(CREATION_TIME_ACTION).text("1 - Creation time").add()
            .newItem(PRIORITY_ACTION).text("2 - Priority").add()
            .newItem(ID_ACTION).text("3 - Id").add()
            .newItem(BACK_ACTION).text("4 - Back").add()
            .addPrompt();

        Map<String, ? extends PromtResultItemIF> result = Try.of(() -> prompt.prompt(promptBuilder.build()))
            .get();

        ListResult action = (ListResult) result.get(ACTION);

        switch (action.getSelectedId()) {
            case CREATION_TIME_ACTION:
                taskManager.listAll(CREATION_TIME)
                    .forEach(System.out::println);
                break;
            case PRIORITY_ACTION:
                taskManager.listAll(PRIORITY)
                    .forEach(System.out::println);
                break;
            case ID_ACTION:
                taskManager.listAll(ID)
                    .forEach(System.out::println);
                break;
            case BACK_ACTION:
                MainScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
                break;
        }

        ListProcessScreen.builder()
            .taskManager(taskManager)
            .build()
            .invoke();

        return null;
    }
}

