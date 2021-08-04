package com.test.taskmanager;

import com.test.taskmanager.app.view.MainScreen;
import com.test.taskmanager.service.impl.TaskManagerImpl;

public class Main {
    public static void main(String... args) {
        new MainScreen(new TaskManagerImpl()).invoke();
    }
}
