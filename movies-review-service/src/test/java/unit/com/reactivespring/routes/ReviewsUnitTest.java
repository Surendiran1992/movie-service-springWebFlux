package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionHandler.ExceptionHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.MovieReviewRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;

@WebFluxTest
@ContextConfiguration(classes = {ReviewHandler.class, ReviewRouter.class, ExceptionHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private MovieReviewRepository movieReviewRepository;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void test_add_endpoint() {

        var movie = new Review(null, null, "Awesome Movie 1", -6.9);

        Mockito.when(movieReviewRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review(null, 1L, "Awesome Movie", 9.0)));


        webTestClient.post().uri("/v1/review").bodyValue(movie)
                .exchange().expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var errorMessage = stringEntityExchangeResult.getResponseBody();
                    var expectedErrorMessage = "rating.negative : please pass a non-negative value,review.movieInfoId must not be null";
                    assert errorMessage != null;
                    assertThat(errorMessage).isEqualTo(expectedErrorMessage);
                });
    }

    @Test
    public void test_update_endpoint() {

        var movie = new Review(null, 45L, "Awesome Movie 1", 6.9);

        Mockito.when(movieReviewRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review(null, 45L, "Awesome Movie 1", 6.9)));

        webTestClient.post().uri("/v1/review").bodyValue(movie)
                .exchange().expectStatus().is2xxSuccessful();

        var id = "100";
        Mockito.when(movieReviewRepository.findById(isA(String.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/v1/review/{id}",id).bodyValue(movie)
                .exchange().expectStatus().isNotFound();
                /*.expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var errorMessage = stringEntityExchangeResult.getResponseBody();
                    var expectedErrorMessage = "rating.negative : please pass a non-negative value,review.movieInfoId must not be null";
                    assert errorMessage != null;
                    assertThat(errorMessage).isEqualTo(expectedErrorMessage);
                });*/
    }
}
