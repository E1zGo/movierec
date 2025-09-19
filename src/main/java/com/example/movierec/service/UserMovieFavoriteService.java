package com.example.movierec.service;

import com.example.movierec.entity.Movie;
import com.example.movierec.entity.User;
import com.example.movierec.entity.UserMovieFavorite;
import com.example.movierec.repository.MovieRepository;
import com.example.movierec.repository.UserMovieFavoriteRepository;
import com.example.movierec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserMovieFavoriteService {

    @Autowired
    private UserMovieFavoriteRepository userMovieFavoriteRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;


    @Transactional
    // 1. 修改返回类型，从 String 改为 Map<String, Object>
    public Map<String, Object> toggleFavorite(Integer movieId, Integer userId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("电影不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Optional<UserMovieFavorite> existingFavorite = userMovieFavoriteRepository
                .findByUserAndMovie(user, movie);

        String message;
        boolean isFavorited;

        if (existingFavorite.isPresent()) {
            // 如果已收藏，则取消收藏
            userMovieFavoriteRepository.delete(existingFavorite.get());
            message = "取消收藏成功";
            isFavorited = false; // 更新状态为 未收藏
        } else {
            // 如果未收藏，则添加收藏
            UserMovieFavorite favorite = new UserMovieFavorite();
            favorite.setUser(user);
            favorite.setMovie(movie);
            userMovieFavoriteRepository.save(favorite);
            message = "收藏成功";
            isFavorited = true; // 更新状态为 已收藏
        }

        // 2. 在操作完成后，获取最新的收藏总数
        //    这里直接使用了您 getFavoriteCount 方法中的核心逻辑
        Long newFavoriteCount = userMovieFavoriteRepository.countByMovie(movie);

        // 3. 将所有需要的数据打包成一个 Map
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("newFavoriteCount", newFavoriteCount);
        result.put("isFavorited", isFavorited);

        return result; // 4. 返回这个 Map
    }

    /**
     * 新增: 获取电影的收藏总数
     * 供 MovieController 调用
     */
    public int getFavoriteCount(Integer movieId) {
        // 先通过 movieId 找到 Movie 实体
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("电影不存在"));

        // 调用 Repository 的 countByMovie 方法，将结果转换为 int 类型
        return userMovieFavoriteRepository.countByMovie(movie).intValue();
    }
}