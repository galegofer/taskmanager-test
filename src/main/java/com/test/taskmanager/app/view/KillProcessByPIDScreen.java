package com.test.taskmanager.app.view;

import static com.test.taskmanager.domain.SortingType.ID;

import com.test.taskmanager.domain.Process;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import de.codeshelf.consoleui.prompt.builder.CheckboxPromptBuilder;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.List;
import java.util.Map;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class KillProcessByPIDScreen extends AbstractScreen<Void> {

    @Override
    public Void invoke() {
        List<Process> processes = taskManager.listAll(ID);

        if (!processes.isEmpty()) {
            CheckboxPromptBuilder builder = promptBuilder.createCheckboxPrompt()
                .name(ACTION)
                .message("Kill Processes by PID:");

            processes
                .forEach(process -> builder.newItem(String.valueOf(process.getPid()))
                    .text("Process PID: " + process.getPid() + " priority: " + process.getPriority())
                    .add());

            builder.addPrompt();

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

            action.getSelectedIds().stream()
                .map(pidsToKill -> taskManager.kill(Long.parseLong(pidsToKill)))
                .forEach(killedProcess -> Option.ofOptional(killedProcess)
                    .onEmpty(() -> System.out.println("No process to kill with given priority"))
                    .peek(process -> System.out.println(process + " killed")));
        } else {
            System.out.println("There is no processes to kill");
        }

        KillProcessScreen.builder()
            .taskManager(taskManager)
            .build()
            .invoke();

        return null;
    }
}

