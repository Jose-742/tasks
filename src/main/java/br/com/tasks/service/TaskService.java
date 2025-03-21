package br.com.tasks.service;

import br.com.tasks.exception.TaskNotFoundException;
import br.com.tasks.messaging.TaskNotificationProducer;
import br.com.tasks.model.Address;
import br.com.tasks.model.Task;
import br.com.tasks.repository.TaskCustomRepository;
import br.com.tasks.repository.TaskRepository;
import ch.qos.logback.classic.spi.IThrowableProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    private final TaskCustomRepository taskCustomRepository;

    private final AddressService addressService;

    private final TaskNotificationProducer producer;

    public TaskService(TaskRepository taskRepository,
                       TaskCustomRepository taskCustomRepository,
                       AddressService addressService,
                       TaskNotificationProducer producer) {
        this.taskRepository = taskRepository;
        this.taskCustomRepository = taskCustomRepository;
        this.addressService = addressService;
        this.producer = producer;
    }

    public Mono<Task> insert(Task task) {
      return Mono.just(task)
              .map(Task::insert)
              .flatMap(this::save)
              .doOnError(error -> LOGGER.error("Error during save task. Title: {}", task.getTitle(), error));
    }

    public Mono<Page<Task>> findPaginated(Task task, Integer page, Integer size) {
        return taskCustomRepository.findPaginated(task, page, size);
    }

    public Mono<Task> update(Task task) {
        return taskRepository.findById(task.getId())
                .map(task::update)
                .flatMap(taskRepository::save)
                .switchIfEmpty(Mono.error(TaskNotFoundException::new))
                .doOnError(error -> LOGGER.error("Error during update task with id: {}, Message: {}", task.getId(), error.getMessage()));
    }

    public Mono<Void> deleteById(String id) {
        return taskRepository.deleteById(id);
    }

    public Mono<Task> start(String id, String zipcode) {
        return taskRepository.findById(id)
                .zipWhen(it -> addressService.getAddress(zipcode))
                .flatMap(it -> updateAddress(it.getT1(), it.getT2()))
                .map(Task::start)
                .flatMap(taskRepository::save)
                .flatMap(producer::sendNotification)
                .switchIfEmpty(Mono.error(TaskNotFoundException::new))
                .doOnError(error -> LOGGER.error("Error on start task, id: {}, Message: {}", id, error.getMessage()));
    }

    public Mono<Task> done(Task task) {
        return Mono.just(task)
                .doOnNext(it -> LOGGER.info("Finished task. ID: {}", task.getId()))
                .map(Task::done)
                .flatMap(taskRepository::save);
    }

    private Mono<Task> updateAddress(Task task, Address address) {
        return Mono.just(task)
                .map(it -> task.updateAddress(address));
    }

    private Mono<Task> save(Task task) {
        return Mono.just(task)
                .doOnNext(t -> LOGGER.info("Saving task with title {}", t.getTitle()))
                .flatMap(taskRepository::save);
    }
}
