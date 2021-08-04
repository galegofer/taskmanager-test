package com.test.taskmanager.app;

import com.test.taskmanager.app.view.MainScreen;
import com.test.taskmanager.service.impl.TaskManagerImpl;

public final class Main {

    public static void main(String... args) {
        MainScreen.builder()
            .taskManager(new TaskManagerImpl())
            .build()
            .invoke();
    }
}
