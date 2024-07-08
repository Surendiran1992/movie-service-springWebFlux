package com.reactivespring.exceptionHandler;

import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handleMovieInfoClientException(MoviesInfoClientException moviesInfoClientException){
        log.error("Exception caught in Exception Handler : {}",moviesInfoClientException.getMessage());
        return ResponseEntity.status(moviesInfoClientException.getStatusCode()).body(moviesInfoClientException.getMessage());
    }

    @ExceptionHandler(MoviesInfoServerException.class)
    public ResponseEntity<String> handleMovieInfoServerException(MoviesInfoServerException moviesInfoServerException){
        log.error("Exception caught in Exception Handler : {}",moviesInfoServerException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(moviesInfoServerException.getMessage());
    }

    @ExceptionHandler(ReviewsClientException.class)
    public ResponseEntity<String> handleReviewClientException(ReviewsClientException reviewsClientException){
        log.error("Exception caught in Exception Handler : {}",reviewsClientException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(reviewsClientException.getMessage());
    }

    @ExceptionHandler(ReviewsServerException.class)
    public ResponseEntity<String> handleReviewsServerException(ReviewsServerException reviewsServerException){
        log.error("Exception caught in Exception Handler : {}",reviewsServerException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(reviewsServerException.getMessage());
    }
}
