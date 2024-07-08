package com.reactivespring.moviesinfoservice.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class FluxAndMonoController {

    @GetMapping(value = "/flux")
    public Flux<Integer> flux(){
        return Flux.just(1,2,3).log();
    }

    //This is a streaming endpoint, that sends response to the client frequently depending on the time interval specifi
    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream(){
        return Flux.interval(Duration.ofSeconds(1)).log();
    }
}
