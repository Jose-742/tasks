package br.com.tasks.controller.converter;

import br.com.tasks.controller.dto.TaskUpdateDTO;
import br.com.tasks.model.Task;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TaskUpdateDTOConverter {

    public Task convert(TaskUpdateDTO dto){
        return Optional.ofNullable(dto)
                .map(source -> Task.builder()
                        .withId(source.getId())
                        .withTitle(source.getTitle())
                        .withDescription(source.getDescription())
                        .withPriority(source.getPriority())
                        .build())
                .orElse(null);
    }
}
