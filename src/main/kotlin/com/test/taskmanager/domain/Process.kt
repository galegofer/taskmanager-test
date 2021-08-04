package com.test.taskmanager.domain

import io.vavr.concurrent.Future
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

data class Process constructor(val priority: Priority,
                          val pid: Long = PIDGenerator.getPid(),
                          private val running: AtomicBoolean = AtomicBoolean(false),
                          val creationTime: LocalDateTime = LocalDateTime.now(),
                          private var future: Future<Void>? = null) : Runnable {

    private val log = KotlinLogging.logger {}

    override fun run() {
        log.debug("Executing task with PID: {}", pid)
        running.set(true)

        while (running.get()) {
            try {
                Thread.sleep(2000)
            } catch (ex: InterruptedException) {
                log.warn("Task with PID: {} was interrupted", pid)
                running.set(false)
            }
        }
    }

    fun getRunning() = running.get()

    fun kill(): Process {
        running.set(false)
        future?.cancel()
        log.info("Killed process with PID: {}, priority: {}", pid, priority)
        return this
    }

    private object PIDGenerator {
        private val pidCounter = AtomicLong(0)
        fun getPid() = pidCounter.incrementAndGet()
    }
}