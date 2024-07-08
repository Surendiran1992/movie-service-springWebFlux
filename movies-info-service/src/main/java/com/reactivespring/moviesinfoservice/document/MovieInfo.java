package com.reactivespring.moviesinfoservice.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MovieInfo {

    @Id
    private String movieId;

    @NotBlank(message = "movieInfo.name should be present")
    private String name;

    @NotNull(message = "movieInfo.yearReleased shouldnt be null")
    @Positive(message = "movieInfo.yearReleased must be positive")
    private int yearReleased;

    private List<@NotBlank(message = "movie.cast cannot be blank") String> cast;

    private LocalDate date_released;
}
