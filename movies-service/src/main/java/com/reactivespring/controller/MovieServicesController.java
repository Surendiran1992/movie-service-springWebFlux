package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoClient;
import com.reactivespring.domain.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movie")
public class MovieServicesController {

    @Autowired
    private MovieInfoClient movieInfoClient;


    @GetMapping("/{id}")
    public Mono<Movie> getMovieInfoAndReviews(@PathVariable String id) {
        return movieInfoClient.retrieveMovieInfo(id)
                .flatMap(movie -> {
                    var movieReviewListMono = movieInfoClient.retrieveReviews(movie.getMovieInfoId()).collectList();
                    return movieReviewListMono.map(review -> new Movie(movie,review));
                });
    }
}
