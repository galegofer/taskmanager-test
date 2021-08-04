package com.test.taskmanager.service.impl;

import static com.test.taskmanager.domain.Priority.HIGH;
import static com.test.taskmanager.domain.Priority.LOW;
import static com.test.taskmanager.domain.Priority.MEDIUM;
import static com.test.taskmanager.domain.SortingType.CREATION_TIME;
import static com.test.taskmanager.domain.SortingType.ID;
import static com.test.taskmanager.domain.SortingType.PRIORITY;
import static com.test.taskmanager.service.impl.TaskManagerImpl.MAX_PROCESSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_MINUTE;

import com.test.taskmanager.domain.Process;
import com.test.taskmanager.service.TaskManager;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TaskManagerImplTest {

    private final Condition<Process> processRunning = new Condition<>(Process::getRunning,
        "Process running");

    private final TaskManager underTest = new TaskManagerImpl();

    @AfterEach
    public void cleanUp() {
        underTest.killAll();
    }

    @Test
    void testKillAll() {
        Optional<Process> process1 = underTest.addWithPriority(LOW);
        await().atMost(ONE_MINUTE).until(() -> process1.get().getRunning());

        assertThat(process1)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process2 = underTest.add(HIGH);
        await().atMost(ONE_MINUTE).until(() -> process2.get().getRunning());

        assertThat(process2)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process3 = underTest.add(MEDIUM);
        await().atMost(ONE_MINUTE).until(() -> process3.get().getRunning());

        assertThat(process3)
            .isPresent()
            .hasValueSatisfying(processRunning);

        List<Process> results = underTest.listAll(CREATION_TIME);

        assertThat(results)
            .isNotEmpty()
            .hasSize(3)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid()));

        List<Process> killedProcesses = underTest.killAll();

        assertThat(killedProcesses)
            .isNotEmpty()
            .hasSize(3)
            .allMatch(value -> !value.getRunning());

        List<Process> emptyResults = underTest.listAll(CREATION_TIME);

        assertThat(emptyResults)
            .isEmpty();
    }

    @Test
    void testKillByPid() {
        Optional<Process> process1 = underTest.addWithPriority(LOW);
        await().atMost(ONE_MINUTE).until(() -> process1.get().getRunning());

        assertThat(process1)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process2 = underTest.add(HIGH);
        await().atMost(ONE_MINUTE).until(() -> process2.get().getRunning());

        assertThat(process2)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process3 = underTest.add(MEDIUM);
        await().atMost(ONE_MINUTE).until(() -> process3.get().getRunning());

        assertThat(process3)
            .isPresent()
            .hasValueSatisfying(processRunning);

        List<Process> results = underTest.listAll(CREATION_TIME);

        assertThat(results)
            .isNotEmpty()
            .hasSize(3)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid()));

        Optional<Process> killedProcess = underTest.kill(process2.get().getPid());

        assertThat(killedProcess)
            .isPresent()
            .matches(value -> !value.get().getRunning());

        List<Process> resultsWithoutProcess2 = underTest.listAll(CREATION_TIME);

        assertThat(resultsWithoutProcess2)
            .isNotEmpty()
            .hasSize(2)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process3.get().getPid()));
    }

    @Test
    void testKillByNullPID() {
        Optional<Process> process1 = underTest.addWithPriority(LOW);
        await().atMost(ONE_MINUTE).until(() -> process1.get().getRunning());

        assertThat(process1)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process2 = underTest.add(HIGH);
        await().atMost(ONE_MINUTE).until(() -> process2.get().getRunning());

        assertThat(process2)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process3 = underTest.add(MEDIUM);
        await().atMost(ONE_MINUTE).until(() -> process3.get().getRunning());

        assertThat(process3)
            .isPresent()
            .hasValueSatisfying(processRunning);


        List<Process> results = underTest.listAll(CREATION_TIME);

        assertThat(results)
            .isNotEmpty()
            .hasSize(3)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid()));

        Optional<Process> killedProcess = underTest.kill((Process) null);

        assertThat(killedProcess)
            .isNotPresent();

        List<Process> sameResults = underTest.listAll(CREATION_TIME);

        assertThat(sameResults)
            .isNotEmpty()
            .hasSize(3)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid()));
    }

    @Test
    void testKillByPriority() {
        Optional<Process> process1 = underTest.add(LOW);
        await().atMost(ONE_MINUTE).until(() -> process1.get().getRunning());

        assertThat(process1)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process2 = underTest.add(HIGH);
        await().atMost(ONE_MINUTE).until(() -> process2.get().getRunning());

        assertThat(process2)
            .isPresent()
            .hasValueSatisfying(processRunning);

        Optional<Process> process3 = underTest.add(HIGH);
        await().atMost(ONE_MINUTE).until(() -> process3.get().getRunning());

        assertThat(process3)
            .isPresent()
            .hasValueSatisfying(processRunning);

        List<Process> results = underTest.listAll(CREATION_TIME);

        assertThat(results)
            .isNotEmpty()
            .hasSize(3)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid()));

        List<Process> killedProcesses = underTest.kill(HIGH);

        assertThat(killedProcesses)
            .isNotEmpty()
            .allMatch(value -> !value.getRunning())
            .allMatch(value -> value.getPriority() == HIGH);

        List<Process> resultsWithoutProcess2 = underTest.listAll(CREATION_TIME);

        assertThat(resultsWithoutProcess2)
            .isNotEmpty()
            .hasSize(1)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid()));
    }

    @Test
    void testListByNull() {
        Optional<Process> process1 = underTest.add(LOW);
        Optional<Process> process2 = underTest.add(MEDIUM);
        Optional<Process> process3 = underTest.add(HIGH);
        Optional<Process> process4 = underTest.add(LOW);
        Optional<Process> process5 = underTest.add(HIGH);

        await().atMost(ONE_MINUTE).until(() -> process4.get().getRunning());

        List<Process> results = underTest.listAll(null);

        assertThat(process5)
            .isNotPresent();

        assertThat(results)
            .isNotEmpty()
            .hasSize(MAX_PROCESSES)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid(),
                process4.get().getPid()));
    }

    @Test
    void testListById() {
        Optional<Process> process1 = underTest.add(LOW);
        Optional<Process> process2 = underTest.add(MEDIUM);
        Optional<Process> process3 = underTest.add(HIGH);
        Optional<Process> process4 = underTest.add(LOW);
        Optional<Process> process5 = underTest.add(HIGH);

        await().atMost(ONE_MINUTE).until(() -> process4.get().getRunning());

        List<Process> results = underTest.listAll(ID);

        assertThat(process5)
            .isNotPresent();

        assertThat(results)
            .isNotEmpty()
            .hasSize(MAX_PROCESSES)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid(),
                process4.get().getPid()));
    }

    @Test
    void testListByCreationTime() {
        Optional<Process> process1 = underTest.add(LOW);
        Optional<Process> process2 = underTest.add(MEDIUM);
        Optional<Process> process3 = underTest.add(HIGH);
        Optional<Process> process4 = underTest.add(LOW);
        Optional<Process> process5 = underTest.add(HIGH);

        await().atMost(ONE_MINUTE).until(() -> process4.get().getRunning());

        List<Process> results = underTest.listAll(CREATION_TIME);

        assertThat(process5)
            .isNotPresent();

        assertThat(results)
            .isNotEmpty()
            .hasSize(MAX_PROCESSES)
            .allMatch(Process::getRunning)
            .extracting(Process::getCreationTime)
            .containsSequence(List.of(process1.get().getCreationTime(), process2.get().getCreationTime(),
                process3.get().getCreationTime(),
                process4.get().getCreationTime()));
    }

    @Test
    void testListByPriority() {
        underTest.add(LOW);
        underTest.add(MEDIUM);
        underTest.add(HIGH);
        Optional<Process> process4 = underTest.add(LOW);
        Optional<Process> process5 = underTest.add(HIGH);

        await().atMost(ONE_MINUTE).until(() -> process4.get().getRunning());

        List<Process> results = underTest.listAll(PRIORITY);

        assertThat(process5)
            .isNotPresent();

        assertThat(results)
            .isNotEmpty()
            .hasSize(MAX_PROCESSES)
            .allMatch(Process::getRunning)
            .extracting(Process::getPriority)
            .containsSequence(List.of(HIGH, MEDIUM, LOW, LOW));
    }

    @Test
    void testAddForSingleProcess() {
        Optional<Process> process = underTest.add(HIGH);

        await().atMost(ONE_MINUTE).until(() -> process.get().getRunning());

        List<Process> processes = underTest.listAll(CREATION_TIME);

        assertThat(processes)
            .isNotEmpty()
            .hasSize(1)
            .allMatch(Process::getRunning)
            .allMatch(value -> value.getPriority() == process.get().getPriority())
            .allMatch(value -> value.getPid() == process.get().getPid());
    }

    @Test
    void testAddForSingleProcessWithNull() {
        assertThatThrownBy(() -> underTest.add(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testAdd() {
        Optional<Process> process1 = underTest.add(HIGH);
        Optional<Process> process2 = underTest.add(LOW);
        Optional<Process> process3 = underTest.add(HIGH);
        Optional<Process> process4 = underTest.add(LOW);

        Optional<Process> process5 = underTest.add(HIGH);
        Optional<Process> process6 = underTest.add(HIGH);

        await().atMost(ONE_MINUTE).until(() -> process4.get().getRunning());

        List<Process> results = underTest.listAll(ID);

        assertThat(process5)
            .isNotPresent();
        assertThat(process6)
            .isNotPresent();

        assertThat(results)
            .isNotEmpty()
            .hasSize(MAX_PROCESSES)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(), process3.get().getPid(),
                process4.get().getPid()));
    }

    @Test
    void testAddEjectingOldsTasks() {
        underTest.addToFifo(HIGH);
        Optional<Process> process2 = underTest.addToFifo(LOW);
        Optional<Process> process3 = underTest.addToFifo(LOW);
        Optional<Process> process4 = underTest.addToFifo(LOW);
        Optional<Process> process5 = underTest.addToFifo(LOW);

        await().atMost(ONE_MINUTE).until(() -> process5.get().getRunning());

        List<Process> results = underTest.listAll(ID);

        assertThat(results)
            .isNotEmpty()
            .hasSize(MAX_PROCESSES)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process2.get().getPid(), process3.get().getPid(),
                process4.get().getPid(), process5.get().getPid()));
    }

    @Test
    void testAddEjectingOldsSeveralHighPriorityTasks() {
        Optional<Process> process1 = underTest.addWithPriority(MEDIUM);
        Optional<Process> process2 = underTest.addWithPriority(LOW);
        Optional<Process> process3 = underTest.addWithPriority(LOW);
        Optional<Process> process4 = underTest.addWithPriority(MEDIUM);
        Optional<Process> process5 = underTest.addWithPriority(HIGH);

        await().atMost(ONE_MINUTE).until(() -> process5.get().getRunning());

        List<Process> results = underTest.listAll(ID);

        assertThat(results)
            .isNotEmpty()
            .hasSize(MAX_PROCESSES)
            .allMatch(Process::getRunning)
            .extracting(Process::getPid)
            .containsSequence(List.of(process1.get().getPid(), process2.get().getPid(),
                process4.get().getPid(), process5.get().getPid()));
    }
}