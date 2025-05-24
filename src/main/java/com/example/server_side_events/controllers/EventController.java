package com.example.server_side_events.controllers;

import com.example.server_side_events.services.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(value = "/stream-flux", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux - " + LocalTime.now());
    }

    @GetMapping(value = "/stream-sse")
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(sequence))
                        .event("periodic-event")
                        .data("SSE - " + LocalTime.now())
                        .build());
    }

    @PostMapping(value = "stream-subscribe-sse")
    public ResponseEntity<Void> subscribeToEvents() {
        eventService.subscribeToEvents();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/stream-sse-mvc")
    public ResponseEntity<SseEmitter> streamSseMvc() {
        SseEmitter sseEmitter = eventService.streamSseMvc();
        return ResponseEntity.ok(sseEmitter);
    }
}
