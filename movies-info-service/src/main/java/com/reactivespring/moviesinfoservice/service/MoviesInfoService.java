package com.reactivespring.moviesinfoservice.service;

import com.reactivespring.moviesinfoservice.dao.MovieInfoRepository;
import com.reactivespring.moviesinfoservice.document.MovieInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo){
        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMovies() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(String id, MovieInfo updateMovieInfo) {
        return movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setCast(updateMovieInfo.getCast());
                    movieInfo.setName(updateMovieInfo.getName());
                    movieInfo.setYearReleased(updateMovieInfo.getYearReleased());
                    movieInfo.setDate_released(updateMovieInfo.getDate_released());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    public Mono<Void> deleteMovieById(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> findMovieInfoByYear(Integer year){
        return movieInfoRepository.findByYearReleased(year);
    }

    public Mono<MovieInfo> findMovieInfoByName(String name){
        return movieInfoRepository.findByName(name);
    }
}
