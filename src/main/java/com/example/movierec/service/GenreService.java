package com.example.movierec.service;

import com.example.movierec.entity.Genre;
import com.example.movierec.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    /**
     * 获取所有电影类型
     */
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    /**
     * 获取热门类型
     */
    public List<Genre> getHotGenres() {
        List<Object[]> results = genreRepository.findHotGenres();
        return results.stream()
                .map(result -> (Genre) result[0])
                .toList();
    }
}