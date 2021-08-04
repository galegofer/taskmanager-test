package com.test.taskmanager.app.view;

import static org.fusesource.jansi.Ansi.ansi;

import com.test.taskmanager.domain.Priority;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import io.vavr.control.Try;
import java.util.Map;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class AddProcessScreen extends AbstractScreen<Void> {

    private static final String REGULAR_ACTION = "regular";
    private static final String FIFO_ACTION = "fifo";
    private static final String PRIORITY_ACTION = "priority";

    @Override
    public Void invoke() {
        promptBuilder.createListPrompt()
            .name(ACTION)
            .message("Add process version:")
            .newItem(REGULAR_ACTION).text("1 - Regular Process").add()
            .newItem(FIFO_ACTION).text("2 - FIFO Process").add()
            .newItem(PRIORITY_ACTION).text("3 - Process with Priority").add()
            .newItem(BACK_ACTION).text("4 - Back").add()
            .addPrompt();

        Map<String, ? extends PromtResultItemIF> result = Try.of(() -> prompt.prompt(promptBuilder.build()))
            .get();

        ListResult action = (ListResult) result.get(ACTION);

        if (BACK_ACTION.equals(action.getSelectedId())) {
            MainScreen.builder()
                .taskManager(taskManager)
                .build()
                .invoke();
        }

        Priority priority = ProcessPriorityScreen.builder()
            .taskManager(taskManager)
            .build()
            .invoke();

        switch (action.getSelectedId()) {
            case REGULAR_ACTION:
                taskManager.add(priority)
                    .ifPresentOrElse(process -> System.out.println(ansi().render(String
                        .format("Created process with PID: %d and priority: %s", process.getPid(),
                            process.getPriority()))), () -> System.out.println(ansi()
                        .render("Process couldn't be created, because the task manager is at limit of processes")));
                break;
            case FIFO_ACTION:
                taskManager.addToFifo(priority)
                    .ifPresent(process -> System.out.println(ansi().render(String
                        .format("Created process with PID: %d and priority: %s for FIFO", process.getPid(),
                            process.getPriority()))));
                break;
            case PRIORITY_ACTION:
                taskManager.addWithPriority(priority)
                    .ifPresent(process -> System.out.println(ansi().render(String
                        .format("Created process with PID: %d and priority: %s killing by priority",
                            process.getPid(), process.getPriority()))));
                break;
            case BACK_ACTION:
                MainScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
                break;
        }

        AddProcessScreen.builder()
            .taskManager(taskManager)
            .build()
            .invoke();

        return null;
    }
}
