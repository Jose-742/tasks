package br.com.tasks.messaging;

import br.com.tasks.model.Task;
import br.com.tasks.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TaskNotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskNotificationConsumer.class);

    private final TaskService taskService;

    public TaskNotificationConsumer(TaskService taskService) {
        this.taskService = taskService;
    }

    // Recebendo e alterando o status da task do kafka
    @KafkaListener(topics = "${kafka.task.notification.output}", groupId = "${kafka.task.notification.group.id}")
    public void receiveAndFinishTask(Task task) {
        Mono.just(task)
                .doOnNext(it -> LOGGER.info("Receive task to finish. ID: {}", task.getId()))
                .flatMap(taskService::done)
                .subscribe();
    }

    // block() - inica um fluxo bloqueando o proximo - uma por vez
    // subscribe() - inicia um fluxo sem bloquear - multiplas tarefas sem bloquear
}
