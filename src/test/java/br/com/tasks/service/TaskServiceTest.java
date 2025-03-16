package br.com.tasks.service;

import br.com.tasks.model.Task;
import br.com.tasks.repository.TaskCustomRepository;
import br.com.tasks.repository.TaskRepository;
import br.com.tasks.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TaskServiceTest {

    @InjectMocks
    private TaskService service;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCustomRepository taskCustomRepository;

    @Test
    void service_mustReturnTask_whenInsertSuccessfully() {
        Task task = TestUtils.buildValidTask();

        when(taskRepository.save(any())).thenReturn(Mono.just(task));

        StepVerifier.create(service.insert(task))
                .then(() -> verify(taskRepository, times(1)).save(any()))
                .expectNext(task)
                .expectComplete();
    }

    @Test
    void service_mustReturnVoid_whenDeleteTaskSuccessfully() {
        when(taskRepository.deleteById(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteById("someId"))
                .then(() -> verify(taskRepository, times(1)).deleteById(anyString()))
                .verifyComplete();
    }

    @Test
    void service_mustReturnTaskPage_whenFindPaginated() {
        Task task = TestUtils.buildValidTask();

        when(taskCustomRepository.findPaginated(any(), anyInt(), anyInt())).thenReturn(Mono.just(Page.empty()));
        Mono<Page<Task>> result = service.findPaginated(task, 0, 10);

        Assertions.assertNotNull(result);

        verify(taskCustomRepository, times(1)).findPaginated(any(), anyInt(), anyInt());

    }
}
