package br.com.tasks.controller;

import br.com.tasks.controller.converter.TaskDTOConverter;
import br.com.tasks.controller.dto.TaskDTO;
import br.com.tasks.model.Task;
import br.com.tasks.model.TaskState;
import br.com.tasks.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService service;
    private final TaskDTOConverter converter;

    public TaskController(TaskService service, TaskDTOConverter converter) {
        this.service = service;
        this.converter = converter;
    }

    @GetMapping
    public Page<TaskDTO> getTasks(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "0") int priority,
            @RequestParam(required = false) TaskState taskState,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return service.findPaginated(converter.convert(id, title, description, priority, taskState), page, size)
                .map(converter::convert);
    }

    @PostMapping
    public Mono<TaskDTO> createTask(@RequestBody Task task) {
        return service.insert(task)
                .map(converter::convert);
    }
}
