package com.test.taskmanager.service

import com.test.taskmanager.domain.Priority
import com.test.taskmanager.domain.Process
import com.test.taskmanager.domain.SortingType
import java.util.*

/**
 * Interface that exposes all the available operation in the Task Manager.
 */
interface TaskManager {
    /**
     * Add a process - adds new processes till there is capacity inside the Task Manager, otherwise it won’t accept any
     * new process and will return an Optional.empty.
     *
     * @param priority [Priority] the priority of the process that will be added.
     * @return [<] The added process or an empty if the max capacity was reached.
     */
    fun add(priority: Priority): Optional<Process>

    /**
     * Add a process - accepts all new processes killing and removing from the Task Manager list the oldest one when the
     * max size is reached.
     *
     * @param priority [Priority] the priority of the process that will be added.
     * @return [<] The added process or an empty if the max capacity was reached.
     */
    fun addToFifo(priority: Priority): Optional<Process>

    /**
     * Adds a process – If the max size is reached it is evaluated if the new process passed in the add() call has a
     * higher priority compared to any of the existing one, removing the lowest priority that is the oldest, otherwise
     * skip it.
     *
     * @param priority [Priority] the priority of the process that will be added.
     * @return [<] The added process or an empty if the max capacity was reached.
     */
    fun addWithPriority(priority: Priority): Optional<Process>

    /**
     * List running processes sorting them by time of creation, priority or id.
     *
     * @param sortingType [SortingType] the type of sorting.
     * @return List the available processes in the system sorted by the provided SortingType.
     */
    fun listAll(sortingType: SortingType?): List<Process>

    /**
     * Kills a specific process by the provided process extracting the PID from it.
     *
     * @param process the process to kill.
     * @return the process that was killed or Optional.empty if not found
     */
    fun kill(process: Process?): Optional<Process?>

    /**
     * Kills a specific process by the provided PID.
     *
     * @param pid the process id.
     * @return the process that was killed or Optional.empty if not found
     */
    fun kill(pid: Long): Optional<Process>

    /**
     * Kills all the processes with the specified priority.
     *
     * @param priority [Priority] the priority of the process that will be killed.
     * @return the list of processes that were killed based on the given priority or an empty list if none was found.
     */
    fun kill(priority: Priority): List<Process>

    /**
     * Kills all running processes.
     *
     * @return the list of processes that were killed or an empty list if none was found.
     */
    fun killAll(): List<Process>
}