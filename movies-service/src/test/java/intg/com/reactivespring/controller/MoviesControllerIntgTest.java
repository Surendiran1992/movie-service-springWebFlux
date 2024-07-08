package com.reactivespring.controller;

import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class MoviesControllerIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovieServicesController movieServicesController;

    private static final String GET_URL = "v1/movie/";
    @Test
    public void test_get_movie(){
        webTestClient.get().uri(GET_URL+"{id}",1).exchange().expectBody(Movie.class)
                .consumeWith(entityResult -> System.out.println(entityResult.getResponseBody()));
    }
}
