package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource( properties = {
        "restClient.movieInfoUrl = http://localhost:8084/v1/movieInfos",
        "restClient.movieReviewUrl = http://localhost:8084/v1/review"
})
public class MoviesControllerWireMockIntgTest {

    @Autowired
    private WebTestClient webTestClient;
    private static final String GET_URL = "/v1/movie/";

    @Test
    public void test_get_movie(){
        var movieInfoId = "abc";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieInfos/"+movieInfoId))
                .willReturn(WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE,"application/json")
                        .withBodyFile("movieinfo.json"))
        );

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE,"application/json")
                        .withBodyFile("reviews.json"))
        );

        webTestClient.get().uri(GET_URL+"{id}",movieInfoId).exchange().expectBody(Movie.class)
                .consumeWith(entityResult -> {
                    var responseBody = entityResult.getResponseBody();
                    System.out.println("\n\nThe response body is "
                            +responseBody);
                    assert responseBody != null;
                    assertThat(responseBody.getMovieInfo().getName()).isEqualTo("Batman Begins");
                    assertThat(responseBody.getReviewList()).hasSize(2);
                });
    }

    @Test
    public void test_getMoviesInfo_404(){
        var movieInfoId = "abc";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieInfos/"+movieInfoId))
                .willReturn(WireMock.aResponse().withStatus(404))
        );

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE,"application/json")
                        .withBodyFile("reviews.json"))
        );

        webTestClient.get().uri(GET_URL+"{id}",movieInfoId).exchange()
                .expectStatus().is4xxClientError().expectBody(String.class)
                .consumeWith(entityResult -> {
                    var responseBody = entityResult.getResponseBody();
                    System.out.println("\n\nThe response body is "
                            +responseBody);
                    assertThat(responseBody).isEqualTo("There is no movie Info available for passed in id "+movieInfoId);
                });

        WireMock.verify(1,WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/movieInfos/"+movieInfoId)));
    }

    @Test
    public void test_getMovieReview_404(){
        var movieInfoId = "abc";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieInfos/"+movieInfoId))
                .willReturn(WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE,"application/json")
                        .withBodyFile("movieinfo.json"))
        );

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withStatus(404))
        );

        webTestClient.get().uri(GET_URL+"{id}",movieInfoId).exchange()
                .expectStatus().is2xxSuccessful().expectBody(Movie.class)
                .consumeWith(entityResult -> {
                    var responseBody = entityResult.getResponseBody();
                    System.out.println("\n__________________________________\nThe response body is "
                            +responseBody+"\n_______________________________\n");
                    assert responseBody != null;
                    assertThat(responseBody.getMovieInfo().getName()).isEqualTo("Batman Begins");
                    assertThat(responseBody.getReviewList()).hasSize(0);
                });
    }

    @Test
    public void test_getMoviesInfo_500(){
        var movieInfoId = "abc";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieInfos/"+movieInfoId))
                .willReturn(WireMock.aResponse().withStatus(500).withBody("Movie Info Service Unavailable"))
        );

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE,"application/json")
                        .withBodyFile("reviews.json"))
        );

        webTestClient.get().uri(GET_URL+"{id}",movieInfoId).exchange()
                .expectStatus().is5xxServerError().expectBody(String.class)
                .consumeWith(entityResult -> {
                    var responseBody = entityResult.getResponseBody();
                    System.out.println("\n\nThe response body is "
                            +responseBody);
                    assertThat(responseBody).isEqualTo("Movie Info Service Unavailable");
                });

        WireMock.verify(4, WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/movieInfos/"+movieInfoId)));
    }

    @Test
    public void test_getMovieReview_500(){
        var movieInfoId = "abc";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/movieInfos/"+movieInfoId))
                .willReturn(WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE,"application/json")
                        .withBodyFile("movieinfo.json"))
        );

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/review"))
                .willReturn(WireMock.aResponse().withStatus(500).withBody("Movies Review Service Unavailable"))
        );

        webTestClient.get().uri(GET_URL+"{id}",movieInfoId).exchange()
                .expectStatus().is5xxServerError().expectBody(String.class)
                .consumeWith(entityResult -> {
                    var responseBody = entityResult.getResponseBody();
                    System.out.println("\n__________________________________\nThe response body is "
                            +responseBody+"\n_______________________________\n");
                    assertThat(responseBody).isEqualTo("Review Server Exp : Movies Review Service Unavailable");
                });

        WireMock.verify(1,WireMock.getRequestedFor(WireMock.urlPathEqualTo("/v1/review")));
    }
}
