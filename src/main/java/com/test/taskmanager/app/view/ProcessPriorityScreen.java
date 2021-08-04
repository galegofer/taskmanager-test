package com.test.taskmanager.app.view;

import static com.test.taskmanager.domain.Priority.HIGH;
import static com.test.taskmanager.domain.Priority.LOW;
import static com.test.taskmanager.domain.Priority.MEDIUM;

import com.test.taskmanager.domain.Priority;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import io.vavr.control.Try;
import java.util.Map;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class ProcessPriorityScreen extends AbstractScreen<Priority> {

    private static final String LOW_ACTION = "low";
    private static final String MEDIUM_ACTION = "medium";
    private static final String HIGH_ACTION = "high";

    @Override
    public Priority invoke() {
        promptBuilder.createListPrompt()
            .name(ACTION)
            .message("Add process priority:")
            .newItem(LOW_ACTION).text("1 - Low").add()
            .newItem(MEDIUM_ACTION).text("2 - Medium").add()
            .newItem(HIGH_ACTION).text("3 - High").add()
            .newItem(BACK_ACTION).text("4 - Back").add()
            .addPrompt();

        Map<String, ? extends PromtResultItemIF> result = Try.of(() -> prompt.prompt(promptBuilder.build()))
            .get();

        ListResult action = (ListResult) result.get(ACTION);

        switch (action.getSelectedId()) {
            case LOW_ACTION:
                return LOW;
            case MEDIUM_ACTION:
                return MEDIUM;
            case HIGH_ACTION:
                return HIGH;
            case BACK_ACTION:
                AddProcessScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
        }

        return LOW;
    }
}
