package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.MovieReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovieReviewRepository movieReviewRepository;

    private static final String MOVIE_REVIEW_URL = "/v1/review";
    @BeforeEach
    void setUp(){
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review("33L", 2L, "Excellent Movie", 8.0));

        movieReviewRepository.saveAll(reviewsList).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieReviewRepository.deleteAll().block();
    }

    @Test
    public void test_add_endpoint() {

        var movie = new Review(null, 45L, "Awesome Movie 1", 6.9);

        webTestClient.post().uri("/v1/review").bodyValue(movie)
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(movieReviewEntity -> {
                    var movieReview = movieReviewEntity.getResponseBody();
                    assertThat(movieReview).isNotNull();
                    assertThat(movieReview.getReviewId()).isNotNull();
                    assertThat(movieReview.getComment()).isEqualTo("Awesome Movie 1");
                });
    }

    @Test
    public void test_get_all_movie_reviews() {
        webTestClient.get().uri("/v1/review").exchange()
                .expectStatus().is2xxSuccessful().expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    public void test_updateMovieInfo() {
        var id = "33";
        var movie = new Review(null, 1L, "Excellent work", 10.0);

        webTestClient.put().uri(MOVIE_REVIEW_URL + "/{id}", id).bodyValue(movie)
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntity -> {
                    var movieInfo = movieInfoEntity.getResponseBody();
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getMovieInfoId()).isNotNull();
                    assertThat(movieInfo.getComment()).isEqualTo("Excellent work");
                });
    }

    @Test
    public void test_deleteMovieInfo() {
        var id = "33L";

        webTestClient.delete().uri(MOVIE_REVIEW_URL + "/{id}", id).exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri(MOVIE_REVIEW_URL).exchange()
                .expectStatus().is2xxSuccessful().expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    public void test_getReviewByMovieId(){
        var movieInfoId = "1";

        webTestClient.get().uri(url -> url.path(MOVIE_REVIEW_URL).queryParam("movieInfoId",movieInfoId).build())
                .exchange().expectStatus().is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
 /*               .consumeWith(movieReviewEntity -> {
                    var movieReview = movieReviewEntity.getResponseBody();
                    assertThat(movieReview).isNotNull();
                    assertThat(movieReview.getReviewId()).isNotNull();
                    assertThat(movieReview.getComment()).isEqualTo("Awesome Movie 1");
                });
*/
    }
}
