package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> routeReviews(ReviewHandler reviewHandler){
        return RouterFunctions.route()
                //.GET("v1/movieReview", req -> ServerResponse.ok().bodyValue("Hello World"))
                .POST("v1/review", reviewHandler::addReview)
                .GET("v1/review",reviewHandler::getReviews)
                .PUT("v1/review/{id}",reviewHandler::updateReview)
                .DELETE("v1/review/{id}",reviewHandler::deleteReview)
                .GET("v1/reviews/stream",reviewHandler::getReviewsStream)
                .build();
    }
}
