package br.com.tasks.controller;

import br.com.tasks.controller.converter.TaskDTOConverter;
import br.com.tasks.controller.converter.TaskInsertDTOConverter;
import br.com.tasks.controller.dto.TaskDTO;
import br.com.tasks.controller.dto.TaskInsertDTO;
import br.com.tasks.model.Task;
import br.com.tasks.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TaskControllerTest {

    @InjectMocks
    private TaskController controller;

    @Mock
    private TaskService service;

    @Mock
    private TaskDTOConverter converter;

    @Mock
    private TaskInsertDTOConverter insertDTOConverter;

    @Test
    public void controller_mustReturnOk_whenSaveSuccessfully(){
        when(service.insert(any())).thenReturn(Mono.just(new Task()));
        when(converter.convert(any(Task.class))).thenReturn(new TaskDTO());

        WebTestClient client = WebTestClient.bindToController(controller).build();

        client.post()
                .uri("/task")
                .bodyValue(new TaskInsertDTO())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskDTO.class);
    }

    @Test
    public void controller_mustReturnOK_whenGetPaginatedSuccessfully(){
        when(service.findPaginated(any(), anyInt(), anyInt())).thenReturn(Mono.just(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)));

        WebTestClient client = WebTestClient.bindToController(controller).build();

        client.get()
                .uri("/task")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskDTO.class);
    }

    @Test
    public void controller_mustReturnNoContent_wenDeleteSuccessfully(){
        String taskId = "any-id";

        when(service.deleteById(any())).thenReturn(Mono.empty());
        WebTestClient client = WebTestClient.bindToController(controller).build();

        client.delete()
                .uri("/task/" + taskId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);
    }

}
