package com.example.movierec.service;

import com.example.movierec.dto.WordCloudDTO;
import com.example.movierec.entity.Genre;
import com.example.movierec.entity.Movie;
import com.example.movierec.entity.User;
import com.example.movierec.repository.GenreRepository;
import com.example.movierec.repository.MovieRepository;
import com.example.movierec.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    /** 获取用户搜索历史 */
    public List<String> getUserSearchHistory(Integer userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        String searchHistoryJson = user.getSearchHistory();
        if (searchHistoryJson == null || searchHistoryJson.isEmpty()) return Collections.emptyList();

        try {
            List<String> history = objectMapper.readValue(searchHistoryJson, new TypeReference<>() {});
            return history.size() > 8 ? history.subList(0, 8) : history;
        } catch (Exception e) {
            System.err.println("Failed to parse search history JSON: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 保存用户搜索历史 */
    public void saveSearchHistory(Integer userId, String query) {
        if (query == null || query.trim().isEmpty()) return;

        final String cleanedQuery = query.trim();
        ObjectMapper objectMapper = new ObjectMapper();

        userRepository.findById(userId).ifPresent(user -> {
            List<String> history;
            String currentHistoryJson = user.getSearchHistory();

            if (currentHistoryJson == null || currentHistoryJson.isEmpty()) {
                history = new ArrayList<>();
            } else {
                try {
                    history = objectMapper.readValue(currentHistoryJson, new TypeReference<>() {});
                } catch (Exception e) {
                    System.err.println("Failed to parse search history JSON: " + e.getMessage());
                    history = new ArrayList<>();
                }
            }

            history.removeIf(s -> s.equalsIgnoreCase(cleanedQuery));
            history.add(0, cleanedQuery);

            if (history.size() > 8) history = history.subList(0, 8);

            try {
                user.setSearchHistory(objectMapper.writeValueAsString(history));
                userRepository.save(user);
            } catch (Exception e) {
                System.err.println("Failed to save search history JSON: " + e.getMessage());
            }
        });
    }

    /** 清空用户搜索历史 */
    public void clearUserSearchHistory(Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setSearchHistory(null);
            userRepository.save(user);
        });
    }

    /** 获取热门搜索词（按电影评分排序，取前10个） */
    public List<String> getHotSearchWords() {
        int limit = 10;
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "avgRating"));
        List<Movie> movies = movieRepository.findAll(pageable).getContent();

        return movies.stream()
                .map(Movie::getTitle)
                .collect(Collectors.toList());
    }

    /** 获取分类发现（电影类型列表） */
    public List<String> getAllGenres() {
        List<Genre> genres = genreRepository.findAll();
        return genres.stream()
                .map(Genre::getGenreName)
                .collect(Collectors.toList());
    }

    /** 获取词云数据 */
    public List<WordCloudDTO> getWordCloudData() {
        List<Genre> genres = genreRepository.findAll();

        List<WordCloudDTO> wordCloud = genres.stream()
                .map(genre -> new WordCloudDTO(genre.getGenreName(), 30 + (int)(Math.random() * 70)))
                .collect(Collectors.toList());

        // 可附加热门电影名
        List<WordCloudDTO> hotMovies = Arrays.asList(
                new WordCloudDTO("肖申克的救赎", 95),
                new WordCloudDTO("阿甘正传", 88),
                new WordCloudDTO("霸王别姬", 92),
                new WordCloudDTO("盗梦空间", 85)
        );
        wordCloud.addAll(hotMovies);
        return wordCloud;
    }
}
