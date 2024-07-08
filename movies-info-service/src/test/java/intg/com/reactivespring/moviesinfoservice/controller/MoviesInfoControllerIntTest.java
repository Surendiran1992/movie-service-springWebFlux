package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.controller.MoviesInfoController;
import com.reactivespring.moviesinfoservice.dao.MovieInfoRepository;
import com.reactivespring.moviesinfoservice.document.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;
    @Autowired
    private WebTestClient webTestClient;
    private static final String MOVIE_INFO_ADD_URL = "/v1/movieInfos/add";
    private static final String MOVIE_INFO_GET_URL = "/v1/movieInfos";

    @BeforeEach
    public void setUp() {
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("12345L", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        //since reactive prog is an async process, we gotta use this blockLast to block next piece of code from executing before the data is setUp
        movieInfoRepository.saveAll(movieinfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    public void test_add_endpoint() {

        var movie = new MovieInfo("1", "The Dark Knight 2",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));

        webTestClient.post().uri(MOVIE_INFO_ADD_URL).bodyValue(movie)
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntity -> {
                    var movieInfo = movieInfoEntity.getResponseBody();
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getMovieId()).isNotNull();
                    assertThat(movieInfo.getName()).isEqualTo("The Dark Knight 2");
                });
    }

    @Test
    public void test_get_all_movies() {
        webTestClient.get().uri(MOVIE_INFO_GET_URL).exchange()
                .expectStatus().is2xxSuccessful().expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    public void test_find_by_id() {
        var id = "12345L";
        webTestClient.get().uri(MOVIE_INFO_GET_URL + "/{id}", id).exchange()
                .expectStatus().is2xxSuccessful().expectBody()
                .jsonPath("$.name", "Dark Knight Rises").hasJsonPath()
                .jsonPath("$.yearReleased").isEqualTo(2012);
    }

    @Test
    public void test_find_by_id_notFound() {
        var id = "abc";
        webTestClient.get().uri(MOVIE_INFO_GET_URL + "/{id}", id).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void test_updateMovieInfo() {
        var id = "12345L";
        var movie = new MovieInfo(null, "The Dark Knight 2",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));

        webTestClient.put().uri(MOVIE_INFO_GET_URL + "/{id}", id).bodyValue(movie)
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntity -> {
                    var movieInfo = movieInfoEntity.getResponseBody();
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getMovieId()).isNotNull();
                    assertThat(movieInfo.getName()).isEqualTo("The Dark Knight 2");
                });
    }

    @Test
    public void test_updateMovieInfo_notFound() {
        var id = "abc";
        var movie = new MovieInfo(null, "The Dark Knight 2",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));

        webTestClient.put().uri(MOVIE_INFO_GET_URL + "/{id}", id).bodyValue(movie)
                .exchange().expectStatus()
                .isNotFound();
    }

    @Test
    public void test_deleteMovieInfo() {
        var id = "12345L";

        webTestClient.delete().uri(MOVIE_INFO_GET_URL + "/{id}", id).exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri(MOVIE_INFO_GET_URL).exchange()
                .expectStatus().is2xxSuccessful().expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

    @Test
    public void test_get_all_movies_By_year() {
        var uri = UriComponentsBuilder.fromUriString(MOVIE_INFO_GET_URL)
                .queryParam("year", 2012).buildAndExpand()
                .toUri();

        webTestClient.get().uri(uri).exchange()
                .expectStatus().is2xxSuccessful().expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    public void test_get_movieInfo_By_Name(){
        var uri = UriComponentsBuilder.fromUriString("/v1/movieInfo")
                .queryParam("name","The Dark Knight").buildAndExpand().toUri();

        webTestClient.get().uri(uri).exchange()
                .expectStatus().is2xxSuccessful().expectBody()
                .jsonPath("$.yearReleased").isEqualTo(2008);
    }

    @Test
    public void test_stream_endpoint() {

        var movie = new MovieInfo("1", "The Dark Knight 2",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));

        webTestClient.post().uri(MOVIE_INFO_ADD_URL).bodyValue(movie)
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntity -> {
                    var movieInfo = movieInfoEntity.getResponseBody();
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getMovieId()).isNotNull();
                    assertThat(movieInfo.getName()).isEqualTo("The Dark Knight 2");
                });

        var movieInfoStream = webTestClient
                .get().uri(MOVIE_INFO_GET_URL+"/stream").exchange()
                .expectStatus().is2xxSuccessful().returnResult(MovieInfo.class).getResponseBody();

        StepVerifier.create(movieInfoStream).assertNext(movieInfo -> {
            assertThat(movieInfo.getMovieId()).isNotNull();
            assertThat(movieInfo.getName()).isEqualTo("The Dark Knight 2");
        }).thenCancel().verify();
    }
}