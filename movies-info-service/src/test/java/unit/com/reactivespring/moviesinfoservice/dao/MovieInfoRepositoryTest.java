package com.reactivespring.moviesinfoservice.dao;

import com.reactivespring.moviesinfoservice.document.MovieInfo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

//this annotation will spin up embedded mongoDb server just for testing
@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    public void setUp(){
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
    public void tearDown(){
        movieInfoRepository.deleteAll().block();
    }

    @Test
    public void test_Db_Connection(){
        var moviesFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void test_findById(){
        var moviesMono = movieInfoRepository.findById("12345L").log();

        StepVerifier.create(moviesMono)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals("Dark Knight Rises",movieInfo.getName());
                    Assertions.assertEquals(2,movieInfo.getCast().size());
                })
                .verifyComplete();
    }

    @Test
    public void save_test(){
        var movieinfos = new MovieInfo(null, "Interstellar",
                        2014, List.of("", ""), LocalDate.parse("2014-06-15"));

        var moviesMono = movieInfoRepository.save(movieinfos).log();

        StepVerifier.create(moviesMono)
                .assertNext(movieInfo -> {
                    Assertions.assertNotNull(movieInfo.getMovieId());
                    Assertions.assertEquals("Interstellar",movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    public void test_update(){
        var moviesMono = movieInfoRepository.findById("12345L").block();
        moviesMono.setYearReleased(2022);
        var moviesMono1 = movieInfoRepository.save(moviesMono).log();

        StepVerifier.create(moviesMono1)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals(2022,movieInfo.getYearReleased());
                    Assertions.assertEquals("Dark Knight Rises",movieInfo.getName());
                    Assertions.assertEquals(2,movieInfo.getCast().size());
                })
                .verifyComplete();
    }

    @Test
    public void test_delete(){
        //block os used here as we are expecting the delete to happen entirely before we get the list of movies
        var deleteMovie = movieInfoRepository.deleteById("12345L").block();
        var moviesFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void test_findByYear(){
        var moviesFlux = movieInfoRepository.findByYearReleased(2012).log();

        StepVerifier.create(moviesFlux)
                //.expectNextCount(1)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals("Dark Knight Rises",movieInfo.getName());
                    Assertions.assertEquals(2,movieInfo.getCast().size());
                })
                .verifyComplete();
    }

    @Test
    public void test_findByName(){
        var moviesMono = movieInfoRepository.findByName("Dark Knight Rises").log();

        StepVerifier.create(moviesMono)
                //.expectNextCount(1)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals("Dark Knight Rises",movieInfo.getName());
                    Assertions.assertEquals(2,movieInfo.getCast().size());
                })
                .verifyComplete();
    }
}