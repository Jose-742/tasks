package br.com.tasks.controller.converter;

import br.com.tasks.controller.dto.TaskDTO;
import br.com.tasks.model.Task;
import br.com.tasks.model.TaskState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TaskDTOConverter {

    public TaskDTO convert(Task task) {
        return Optional.ofNullable(task)
                .map(source -> {
                    TaskDTO dto = new TaskDTO();
                    dto.setId(source.getId());
                    dto.setTitle(source.getTitle());
                    dto.setDescription(source.getDescription());
                    dto.setPriority(source.getPriority());
                    dto.setState(source.getState());
                    dto.setAddress(source.getAddress());
                    dto.setCreated(source.getCreated());
                    return dto;
                })
                .orElse(null);
    }

    public Task convert(TaskDTO taskDTO) {
        return Optional.ofNullable(taskDTO)
                .map(source -> Task.builder()
                        .withId(source.getId())
                        .withTitle(source.getTitle())
                        .withDescription(source.getDescription())
                        .withPriority(source.getPriority())
                        .withState(source.getState())
                        .build())
                .orElse(null);
    }

    public Task convert(String id, String title, String description, int priority, TaskState state) {
        return Task.builder()
                .withId(id)
                .withTitle(title)
                .withDescription(description)
                .withPriority(priority)
                .withState(state)
                .build();
    }

    public List<TaskDTO> convertList(List<Task> taskList) {
        return Optional.ofNullable(taskList)
                .map(array -> array.stream().map(this::convert).collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }
}
