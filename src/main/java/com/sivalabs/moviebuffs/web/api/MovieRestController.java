package com.sivalabs.moviebuffs.web.api;

import com.sivalabs.moviebuffs.entity.Genre;
import com.sivalabs.moviebuffs.models.MovieDTO;
import com.sivalabs.moviebuffs.models.MoviesResponse;
import com.sivalabs.moviebuffs.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MovieRestController {
    private static final int DEFAULT_PAGE_SIZE = 25;

    private final MovieService movieService;

    @GetMapping("/movies")
    public MoviesResponse getMovies(
            @RequestParam(name = "genre", required = false) String genreSlug,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault.SortDefaults({@SortDefault(sort = "title", direction = ASC)})
            Pageable pageable) {
        log.info("API Fetching movies for page {}", pageable.getPageNumber());
        Page<MovieDTO> moviesPage;
        if(StringUtils.isNotBlank(genreSlug)) {
            moviesPage = getMoviesByGenreSlug(genreSlug, pageable);
        } else {
            moviesPage = movieService.getMovies(pageable);
        }
        log.info("Current page {}", moviesPage.getNumber());
        return new MoviesResponse(moviesPage);
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        Optional<MovieDTO> movieById = movieService.getMovieById(id);
        return movieById.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/genres")
    public List<Genre> getGenres() {
        return movieService.findAllGenres(Sort.by("name"));
    }

    private Page<MovieDTO> getMoviesByGenreSlug(String genreSlug, Pageable pageable) {
        Optional<Genre> byId = movieService.findGenreBySlug(genreSlug);
        return byId.map(genre -> movieService.findMoviesByGenre(genre.getId(), pageable))
                .orElseGet(() -> new PageImpl<>(new ArrayList<>()));
    }
}