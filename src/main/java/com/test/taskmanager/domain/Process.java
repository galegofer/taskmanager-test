package com.test.taskmanager.domain;

import io.vavr.concurrent.Future;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@AllArgsConstructor
@EqualsAndHashCode
public class Process implements Runnable {

    long pid;
    @Exclude
    Priority priority;
    @Exclude
    AtomicBoolean running;
    @Exclude
    LocalDateTime creationTime;
    @With
    @Exclude
    @ToString.Exclude
    Future<Void> future;

    @Builder
    public Process(@NonNull Priority priority) {
        this.pid = PIDGenerator.getPid();
        this.priority = priority;
        this.creationTime = LocalDateTime.now();
        this.running = new AtomicBoolean(false);
        this.future = null;
    }

    @Override
    public void run() {
        log.debug("Executing task with PID: {}", pid);
        running.set(true);

        while (running.get()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                log.warn("Task with PID: {} was interrupted", pid);
                running.set(false);
            }
        }
    }

    public boolean getRunning() {
        return running.get();
    }

    public Process kill() {
        running.set(false);
        future.cancel();

        log.info("Killed process with PID: {}, priority: {}", pid, priority);

        return this;
    }

    private static final class PIDGenerator {

        private static final AtomicLong pidCounter = new AtomicLong(0);

        public static long getPid() {
            return pidCounter.incrementAndGet();
        }
    }
}
