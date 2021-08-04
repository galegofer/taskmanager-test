package com.test.taskmanager.service;

import com.test.taskmanager.domain.Priority;
import com.test.taskmanager.domain.Process;
import com.test.taskmanager.domain.SortingType;
import java.util.List;
import java.util.Optional;

/**
 * Interface that exposes all the available operation in the Task Manager.
 */
public interface TaskManager {

    /**
     * Add a process - adds new processes till there is capacity inside the Task Manager, otherwise it won’t accept any
     * new process and will return an Optional.empty.
     *
     * @param priority {@link Priority} the priority of the process that will be added.
     * @return {@link Optional<Process>} The added process or an empty if the max capacity was reached.
     */
    Optional<Process> add(Priority priority);

    /**
     * Add a process - accepts all new processes killing and removing from the Task Manager list the oldest one when the
     * max size is reached.
     *
     * @param priority {@link Priority} the priority of the process that will be added.
     * @return {@link Optional<Process>} The added process or an empty if the max capacity was reached.
     */
    Optional<Process> addToFifo(Priority priority);

    /**
     * Adds a process – If the max size is reached it is evaluated if the new process passed in the add() call has a
     * higher priority compared to any of the existing one, removing the lowest priority that is the oldest, otherwise
     * skip it.
     *
     * @param priority {@link Priority} the priority of the process that will be added.
     * @return {@link Optional<Process>} The added process or an empty if the max capacity was reached.
     */
    Optional<Process> addWithPriority(Priority priority);

    /**
     * List running processes sorting them by time of creation, priority or id.
     *
     * @param sortingType {@link SortingType} the type of sorting.
     * @return List the available processes in the system sorted by the provided SortingType.
     */
    List<Process> listAll(SortingType sortingType);

    /**
     * Kills a specific process by the provided process extracting the PID from it.
     *
     * @param process the process to kill.
     * @return the process that was killed or Optional.empty if not found
     */
    Optional<Process> kill(Process process);

    /**
     * Kills a specific process by the provided PID.
     *
     * @param pid the process id.
     * @return the process that was killed or Optional.empty if not found
     */
    Optional<Process> kill(long pid);

    /**
     * Kills all the processes with the specified priority.
     *
     * @param priority {@link Priority} the priority of the process that will be killed.
     * @return the list of processes that were killed based on the given priority or an empty list if none was found.
     */
    List<Process> kill(Priority priority);

    /**
     * Kills all running processes.
     *
     * @return the list of processes that were killed or an empty list if none was found.
     */
    List<Process> killAll();
}
