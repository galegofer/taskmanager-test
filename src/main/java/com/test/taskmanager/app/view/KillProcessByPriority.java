package com.test.taskmanager.app.view;

import static com.test.taskmanager.domain.Priority.HIGH;
import static com.test.taskmanager.domain.Priority.LOW;
import static com.test.taskmanager.domain.Priority.MEDIUM;
import static com.test.taskmanager.domain.SortingType.ID;

import com.test.taskmanager.domain.Priority;
import com.test.taskmanager.domain.Process;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import io.vavr.control.Try;
import java.util.Map;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class KillProcessByPriority extends AbstractScreen<Void> {

    private static final String LOW_ACTION = "low";
    private static final String MEDIUM_ACTION = "medium";
    private static final String HIGH_ACTION = "high";

    @Override
    public Void invoke() {
        promptBuilder.createCheckboxPrompt()
            .name(ACTION)
            .message("Kill by Priority:")
            .newItem(LOW_ACTION).text("Low").add()
            .newItem(MEDIUM_ACTION).text("Medium").add()
            .newItem(HIGH_ACTION).text("High").add()
            .addPrompt();

        Map<String, ? extends PromtResultItemIF> result = Try.of(() -> prompt.prompt(promptBuilder.build()))
            .get();

        CheckboxResult action = (CheckboxResult) result.get(ACTION);

        if (action.getSelectedIds().isEmpty()) {
            System.out.println("No process selected to kill");
            KillProcessScreen.builder()
                .taskManager(taskManager)
                .build()
                .invoke();
        }

        action.getSelectedIds()
            .forEach(selectedId -> {
                switch (selectedId) {
                    case LOW_ACTION:
                        killTaskByPriority(LOW);
                        break;
                    case MEDIUM_ACTION:
                        killTaskByPriority(MEDIUM);
                        break;
                    case HIGH_ACTION:
                        killTaskByPriority(HIGH);
                        break;
                }
            });

        KillProcessScreen.builder()
            .taskManager(taskManager)
            .build()
            .invoke();

        return null;
    }

    private void killTaskByPriority(Priority priority) {
        taskManager.listAll(ID).stream()
            .map(Process::getPriority)
            .filter(currentPriority -> currentPriority.equals(priority))
            .forEach(taskManager::kill);
    }
}

