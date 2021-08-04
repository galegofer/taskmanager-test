package com.test.taskmanager.app.view;

import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import io.vavr.control.Try;
import java.util.Map;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class KillProcessScreen extends AbstractScreen<Void> {

    private static final String PID_ACTION = "pid";
    private static final String PRIORITY_ACTION = "priority";
    private static final String ALL_ACTION = "all";

    @Override
    public Void invoke() {
        promptBuilder.createListPrompt()
            .name(ACTION)
            .message("Kill Processes:")
            .newItem(PID_ACTION).text("1 - Kill process by PID").add()
            .newItem(PRIORITY_ACTION).text("2 - Kill processes by priority").add()
            .newItem(ALL_ACTION).text("3 - Kill all the processes").add()
            .newItem(BACK_ACTION).text("4 - Back").add()
            .addPrompt();

        Map<String, ? extends PromtResultItemIF> result = Try.of(() -> prompt.prompt(promptBuilder.build()))
            .get();

        ListResult action = (ListResult) result.get(ACTION);

        switch (action.getSelectedId()) {
            case PID_ACTION:
                KillProcessByPIDScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
            case PRIORITY_ACTION:
                KillProcessByPriority.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
            case ALL_ACTION:
                System.out.println("ALl processes killed: ");
                taskManager.killAll().forEach(killedProcess -> System.out.println(killedProcess + " killed"));
                break;
            case BACK_ACTION:
                MainScreen.builder()
                    .taskManager(taskManager)
                    .build()
                    .invoke();
                break;
        }

        KillProcessScreen.builder()
            .taskManager(taskManager)
            .build()
            .invoke();

        return null;
    }
}

