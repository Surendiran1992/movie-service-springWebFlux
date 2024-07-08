package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.MovieReviewRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    Sinks.Many<Review> reviewSink = Sinks.many().replay().all();
    @Autowired
    private MovieReviewRepository movieReviewRepository;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(movieReviewRepository::save)
                .doOnNext(review -> reviewSink.tryEmitNext(review))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue).log();
    }

    private void validate(Review review) {
        var constraintVoilationSet = validator.validate(review);
        if (!constraintVoilationSet.isEmpty()) {
            var errorMessage = constraintVoilationSet.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted().collect(Collectors.joining(","));
            log.error("contraintViolation {}", errorMessage);
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var querParam = serverRequest.queryParam("movieInfoId");
        if (querParam.isPresent()) {
            var reviewFlux = movieReviewRepository.findReviewsByMovieInfoId(Long.valueOf(querParam.get()))
                    .switchIfEmpty(Flux.error(new ReviewNotFoundException("Review Not Found for given movieId :"+querParam.get())));
            return ServerResponse.ok().body(reviewFlux, Review.class).log();
        } else {
            var reviewFlux = movieReviewRepository.findAll();
            return ServerResponse.ok().body(reviewFlux, Review.class).log();
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {
        var pathVar = serverRequest.pathVariable("id");
        var existingReview = movieReviewRepository.findById(pathVar)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review Not Found for given review id :"+pathVar)));

        return existingReview.flatMap(review -> serverRequest.bodyToMono(Review.class)
                        .map(req -> {
                            review.setComment(req.getComment());
                            review.setRating(req.getRating());
                            review.setMovieInfoId(req.getMovieInfoId());
                            return review;
                        }).flatMap(movieReviewRepository::save))
                .flatMap(ServerResponse.status(HttpStatus.ACCEPTED)::bodyValue).log();
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        var pathvar = serverRequest.pathVariable("id");
        var existingReview = movieReviewRepository.findById(pathvar);

        return existingReview.flatMap(review -> movieReviewRepository.delete(review)
                .then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(reviewSink.asFlux(),Review.class).log();
    }
}
