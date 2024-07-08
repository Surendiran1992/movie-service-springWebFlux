package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.document.MovieInfo;
import com.reactivespring.moviesinfoservice.exceptionHandler.GlobalExceptionHandler;
import com.reactivespring.moviesinfoservice.service.MoviesInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {MoviesInfoController.class})
//@ContextConfiguration(classes = {MoviesInfoController.class,GlobalExceptionHandler.class})
@AutoConfigureWebTestClient
public class MoviesInfoControllerTest {

    @MockBean
    private MoviesInfoService moviesInfoService;

    @Autowired
    private WebTestClient webTestClient;
    private static final String MOVIE_INFO_ADD_URL = "/v1/movieInfos/add";

    private static final String MOVIE_INFO_GET_URL = "/v1/movieInfos";
    private static List<MovieInfo> MOVIE_INFO_FLUX = null;


    @BeforeEach
    public void setUp(){
        MOVIE_INFO_FLUX = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("12345L", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
    }
    @Test
    void test_getAllMovieInfo(){
        when(moviesInfoService.getAllMovies()).thenReturn(Flux.fromIterable(MOVIE_INFO_FLUX));

        webTestClient.get().uri(MOVIE_INFO_GET_URL).exchange()
                .expectStatus().is2xxSuccessful().expectBodyList(MovieInfo.class)
                .hasSize(3);

        when(moviesInfoService.getAllMovies()).thenReturn(Flux.fromIterable(MOVIE_INFO_FLUX));
    }

    @Test
    void test_getMovieInfoById(){
        var id = "12345L";
        when(moviesInfoService.getMovieInfoById(id)).thenReturn(Mono.just(MOVIE_INFO_FLUX.get(2)));

        webTestClient.get().uri(MOVIE_INFO_GET_URL+"/{id}",id).exchange()
                .expectStatus().is2xxSuccessful().expectBody()
                .jsonPath("$.name","Dark Knight Rises").hasJsonPath()
                .jsonPath("$.yearReleased").isEqualTo(2012);
    }

    @Test
    void test_addMovieInfo_validation(){
        var movie = new MovieInfo("mockId", "",
                -2008, List.of("", "HeathLedger"), LocalDate.parse("2008-07-18"));

        when(moviesInfoService.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movie));

        webTestClient.post().uri(MOVIE_INFO_ADD_URL).bodyValue(movie)
                .exchange().expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult ->{
                    var errorMessage = stringEntityExchangeResult.getResponseBody();
                    var expectedErrorMessage = "movie.cast cannot be blank, movieInfo.name should be present, movieInfo.yearReleased must be positive";
                    assert errorMessage!=null;
                    assertThat(errorMessage).isEqualTo(expectedErrorMessage);
                });
    }

    @Test
    public void test_updateMovieInfo(){
        var movie = new MovieInfo("mockId", "The Dark Knight 2",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));

        when(moviesInfoService.updateMovieInfo(isA(String.class),isA(MovieInfo.class))).thenReturn(Mono.just(movie));

        webTestClient.put().uri(MOVIE_INFO_GET_URL+"/{id}","mockId").bodyValue(movie)
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntity ->{
                    var movieInfo = movieInfoEntity.getResponseBody();
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getMovieId()).isNotNull();
                    assertThat(movieInfo.getName()).isEqualTo("The Dark Knight 2");
                });
    }

    @Test
    public void test_deleteMovieInfo(){
        var id = "12345L";

        when(moviesInfoService.deleteMovieById(isA(String.class))).thenReturn(Mono.empty());

        webTestClient.delete().uri(MOVIE_INFO_GET_URL+"/{id}",id).exchange()
                .expectStatus().isNoContent();

    }

}