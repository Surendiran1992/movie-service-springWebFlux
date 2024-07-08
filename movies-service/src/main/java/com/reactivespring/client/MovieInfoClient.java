package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MovieInfoClient {


    private final WebClient movieInfoWebClient;

    private final WebClient movieReviewWebClient;

    public Mono<MovieInfo> retrieveMovieInfo(String movieInfoId) {
        
        var retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(ex -> ex instanceof MoviesInfoServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure()));

        return movieInfoWebClient.get().uri("/{id}", movieInfoId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException(
                                "There is no movie Info available for passed in id " + movieInfoId,
                                clientResponse.statusCode().value()));
                    }
                    return clientResponse.bodyToMono(String.class).flatMap(responseMsg ->
                        Mono.error(new MoviesInfoClientException(responseMsg, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatusCode::is5xxServerError,clientResponse ->
                    clientResponse.bodyToMono(String.class).flatMap(responseMsg ->
                            Mono.error(new MoviesInfoServerException(responseMsg)))
                )
                .bodyToMono(MovieInfo.class)
                //.retry(3)
                .retryWhen(retrySpec)
                .log();
    }

    public Flux<Review> retrieveReviews(String movieInfoId) {
        return movieReviewWebClient.get()
                .uri(uri -> uri.queryParam("movieInfoId", movieInfoId).build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMsg -> Mono.error(new ReviewsClientException(responseMsg)));
                })
                .onStatus(HttpStatusCode::is5xxServerError,clientResponse ->
                    clientResponse.bodyToMono(String.class).flatMap(responseMsg ->
                            Mono.error(new ReviewsServerException("Review Server Exp : "+responseMsg)))
                )
                .bodyToFlux(Review.class).log();
    }
}
