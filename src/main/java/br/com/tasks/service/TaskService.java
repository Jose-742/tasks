package br.com.tasks.service;

import br.com.tasks.model.Task;
import br.com.tasks.repository.TaskCustomRepository;
import br.com.tasks.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskCustomRepository taskCustomRepository;

    public TaskService(TaskRepository taskRepository, TaskCustomRepository taskCustomRepository) {
        this.taskRepository = taskRepository;
        this.taskCustomRepository = taskCustomRepository;
    }

    public Mono<Task> insert(Task task) {
      return Mono.just(task)
              .map(Task::insert)
              .flatMap(this::save);
    }

    public Page<Task> findPaginated(Task task, Integer page, Integer size) {
        return taskCustomRepository.findPaginated(task, page, size);
    }

    private Mono<Task> save(Task task) {
        return Mono.just(task)
                .map(taskRepository::save);
    }

    public Mono<Void> deleteById(String id) {
        return Mono.fromCallable(() -> {
            taskRepository.deleteById(id);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
