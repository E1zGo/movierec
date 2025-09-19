package com.example.movierec.service;

import com.example.movierec.entity.Movie;
import com.example.movierec.entity.User;
import com.example.movierec.entity.UserMovieRating;
import com.example.movierec.repository.MovieRepository;
import com.example.movierec.repository.UserMovieRatingRepository;
import com.example.movierec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class UserMovieRatingService {

    @Autowired
    private UserMovieRatingRepository userMovieRatingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void addOrUpdateRating(Integer movieId, Integer userId, BigDecimal score) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("电影不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否已经评过分
        Optional<UserMovieRating> existingRating = userMovieRatingRepository
                .findByUserAndMovie(user, movie);

        if (existingRating.isPresent()) {
            // 更新现有评分
            UserMovieRating rating = existingRating.get();
            rating.setRating(score);
            userMovieRatingRepository.save(rating);
        } else {
            // 创建新评分
            UserMovieRating rating = new UserMovieRating();
            rating.setUser(user);
            rating.setMovie(movie);
            rating.setRating(score);
            userMovieRatingRepository.save(rating);
        }

        // 重新计算电影的平均评分
        updateMovieAverageRating(movie);
    }

    private void updateMovieAverageRating(Movie movie) {
        List<UserMovieRating> ratings = userMovieRatingRepository.findByMovie(movie);

        if (ratings.isEmpty()) {
            movie.setAvgRating(BigDecimal.ZERO);
            movie.setRatingCount(0);
        } else {
            BigDecimal sum = ratings.stream()
                    .map(UserMovieRating::getRating)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal average = sum.divide(new BigDecimal(ratings.size()), 2, RoundingMode.HALF_UP);

            movie.setAvgRating(average);
            movie.setRatingCount(ratings.size());
        }

        movieRepository.save(movie);
    }

    public Optional<BigDecimal> getUserRatingForMovie(Integer userId, Integer movieId) {
        User user = userRepository.findById(userId).orElse(null);
        Movie movie = movieRepository.findById(movieId).orElse(null);

        if (user == null || movie == null) {
            return Optional.empty();
        }

        return userMovieRatingRepository.findByUserAndMovie(user, movie)
                .map(UserMovieRating::getRating);
    }
}