package br.com.tasks.service;

import br.com.tasks.exception.TaskNotFoundException;
import br.com.tasks.messaging.TaskNotificationProducer;
import br.com.tasks.model.Address;
import br.com.tasks.model.Task;
import br.com.tasks.repository.TaskCustomRepository;
import br.com.tasks.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;


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

    public Mono<List<Task>> doneMany(List<String> ids) {
        return Flux.fromIterable(ids)
                .flatMap(taskRepository::findById)
                    .map(Task::done)
                    .flatMap(taskRepository::save)
                    .doOnNext(it -> LOGGER.info("Done task.. ID: {}", it.getId())
                ).collectList();
    }

    public Flux<Task> refreshCreated(){
        return taskRepository.findAll()
                .filter(Task::createdIsEmpty)
                .map(Task::createdNow)
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

    // ao instanciar a classe ele incia o metodo
    // a cada um dia ele executa o Flux, se tiver tasks executando a mais de 7 dias sem finalizar ele finaliza!
    @PostConstruct
    private void scheduleDoneOlderTasks() {
        Mono.delay(Duration.ofSeconds(5))
                .doOnNext(it -> LOGGER.info("Starting task monitoring"))
                .subscribe();

        Flux.interval(Duration.ofDays(1))
                .flatMap(it -> doneOlderTasks())
                 .doOnNext(tasks -> LOGGER.info("{} task(s) completed after being active for over 7 days.", tasks))
                .subscribe();
    }

    private Mono<Long> doneOlderTasks() {
        return taskCustomRepository.updateStateToDoneForOlderTasks(LocalDate.now().minusDays(7));
    }
}
