package br.com.tasks.utils;

import br.com.tasks.controller.dto.TaskDTO;
import br.com.tasks.model.Task;
import br.com.tasks.model.TaskState;

public class TestUtils {

    public static Task buildValidTask() {
        return Task.builder()
                .withId("123")
                .withTitle("title")
                .withDescription("Description")
                .withPriority(1)
                .withState(TaskState.INSERT)
                .build();
    }

    public static TaskDTO buildValidTaskDTO() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId("123");
        taskDTO.setTitle("title");
        taskDTO.setDescription("Description");
        taskDTO.setPriority(1);
        taskDTO.setState(TaskState.INSERT);
        return taskDTO;
    }
}
