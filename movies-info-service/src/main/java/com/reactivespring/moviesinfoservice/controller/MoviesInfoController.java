package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.document.MovieInfo;
import com.reactivespring.moviesinfoservice.service.MoviesInfoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/v1/movieInfos")
@Slf4j
public class MoviesInfoController {

    private Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();
    @Autowired
    private MoviesInfoService moviesInfoService;

    @PostMapping("add")
    @ResponseStatus(HttpStatus.CREATED)
    private Mono<MovieInfo> addNewMovieInfo(@RequestBody @Valid MovieInfo movieInfo){
        return moviesInfoService.addMovieInfo(movieInfo)
                .doOnNext(movieInfo1 -> movieInfoSink.tryEmitNext(movieInfo1)).log();
    }

    @GetMapping(value = "stream",produces = MediaType.APPLICATION_NDJSON_VALUE)
    private Flux<MovieInfo> getMovieInfoStream(){
        return movieInfoSink.asFlux().log();
    }


    @GetMapping
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value="year",required = false) Integer year){
        if(year!=null){
            log.info("The year in query param is {}",year);
            return moviesInfoService.findMovieInfoByYear(year).log();
        }
        return moviesInfoService.getAllMovies().log();
    }

    @GetMapping("/{id}")
    private Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id){
        return moviesInfoService.getMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    /**
     *when using @ResponseStatus annotation on an exception class, or when setting the reason attribute of this annotation, the HttpServletResponse.sendError method will be used.
     * With HttpServletResponse.sendError, the response is considered complete and should not be written to any further. Furthermore, the Servlet container will typically write an HTML error page therefore making the use of a reason unsuitable for REST APIs.
     * For such cases it is preferable to use a org.springframework.http.ResponseEntity as a return type and avoid the use of @ResponseStatus altogether.
     */
    @PutMapping("/{id}")
    private Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@PathVariable String id, @RequestBody MovieInfo updateMovieInfo){
        return moviesInfoService.updateMovieInfo(id,updateMovieInfo)
                .map(ResponseEntity.status(HttpStatus.OK)::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private Mono<Void> deleteMovieinfo(@PathVariable String id){
        return moviesInfoService.deleteMovieById(id).log();
    }

    @GetMapping("movieInfo")
    private Mono<ResponseEntity<MovieInfo>> getMovieInfoByName(@RequestParam(value = "name",required = true) String name){
        return moviesInfoService.findMovieInfoByName(name)
                .map(ResponseEntity.status(HttpStatus.OK)::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }
}
