package com.reactivespring.moviesinfoservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired
    public WebTestClient webTestClient;

    @Test
    void flux() {
        webTestClient.get().uri("/flux")
                .exchange().expectStatus().is2xxSuccessful()
                .expectBodyList(Integer.class).hasSize(3);
    }

    @Test
    void flux_approach_2(){
       var flux = webTestClient.get().uri("/flux")
                .exchange().expectStatus().is2xxSuccessful()
                .returnResult(Integer.class).getResponseBody();

        StepVerifier.create(flux)
                .expectNext(1,2,3).verifyComplete();
    }

    @Test
    void stream_test(){
        var flux = webTestClient.get().uri("/stream")
                .exchange().expectStatus().is2xxSuccessful()
                .returnResult(Long.class).getResponseBody();

        StepVerifier.create(flux)
                .expectNext(0L,1L,2L,3L)
                .thenCancel()
                .verify();
    }
}