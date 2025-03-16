package br.com.tasks.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException() {
        super("Task not found");
    }
}
