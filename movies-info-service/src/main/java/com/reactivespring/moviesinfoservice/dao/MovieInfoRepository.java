package com.reactivespring.moviesinfoservice.dao;

import com.reactivespring.moviesinfoservice.document.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo,String> {

    Flux<MovieInfo> findByYearReleased(Integer year);

    Mono<MovieInfo> findByName(String name);
}
