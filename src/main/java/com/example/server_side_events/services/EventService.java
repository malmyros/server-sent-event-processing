package com.example.server_side_events.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final ExecutorService executorService;

    public EventService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void subscribeToEvents() {

        String url = "http://localhost:8080/api/v1/events";
        WebClient webClient = WebClient.create(url);

        ParameterizedTypeReference<ServerSentEvent<String>> type =
                new ParameterizedTypeReference<>() {
                };

        Flux<ServerSentEvent<String>> eventStream = webClient.get()
                .uri("/stream-sse")
                .retrieve()
                .bodyToFlux(type);

        eventStream.subscribe(
                content ->
                        logger.info("Time: {} - event: name[{}], id [{}], content[{}] ",
                                LocalTime.now(), content.event(), content.id(), content.data()),
                error -> logger.error("Error receiving SSE", error),
                () -> logger.info("Completed!!!"));
    }

    public SseEmitter streamSseMvc() {

        SseEmitter sseEmitter = new SseEmitter();
        executorService.execute(() -> {
            try {

                int i = 0;
                while (true) {

                    SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                            .id(String.valueOf(i))
                            .data("SSE MVC - " + LocalTime.now())
                            .name("sse event - mvc")
                                    .reconnectTime(Duration.ofSeconds(3).toMillis());

                    sseEmitter.send(sseEventBuilder);
                    Thread.sleep(Duration.ofSeconds(1));
                    i++;
                }

            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
        });

        return sseEmitter;
    }
}
