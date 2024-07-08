package com.reactivespring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Configuration
public class MovieServiceConfig {

    @Value("${restClient.movieInfoUrl}")
    private String MOVIE_INFO_URL;

    @Value("${restClient.movieReviewUrl}")
    private String MOVIE_REVIEW_URL;

    @Bean
    public WebClient movieInfoWebClient(){
        return WebClient.builder().baseUrl(MOVIE_INFO_URL).build();
    }

    @Bean
    public WebClient movieReviewWebClient(){
        return WebClient.builder().baseUrl(MOVIE_REVIEW_URL).build();
    }
}
