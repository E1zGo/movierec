package com.example.movierec.controller;



import com.example.movierec.dto.ApiResponse;

import com.example.movierec.dto.CommentDto;

import com.example.movierec.dto.MovieDTO;

import com.example.movierec.dto.MovieSearchDTO;

import com.example.movierec.entity.Movie;
import com.example.movierec.service.CommentService;

import com.example.movierec.service.MovieService;

import com.example.movierec.service.UserMovieFavoriteService;

import com.example.movierec.service.UserMovieRatingService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;

import org.springframework.web.bind.annotation.*;



import java.math.BigDecimal;

import java.util.List;

import java.util.Map;

import java.util.Optional;



@RestController

@RequestMapping("/api/movies")

@CrossOrigin(origins = "http://localhost:8081")

public class MovieController {
    @Autowired
    private MovieService movieService;

    @Autowired
    private UserMovieRatingService userMovieRatingService;

    @Autowired
    private UserMovieFavoriteService userMovieFavoriteService;

    @Autowired
    private CommentService commentService;
    /**
     * 获取热门电影
     */
    @GetMapping("/hot")
    public ApiResponse<List<MovieDTO>> getHotMovies(
            @RequestParam(defaultValue = "10") int limit) {
        List<MovieDTO> movies = movieService.getHotMovies(limit);
        return ApiResponse.success(movies);
    }
    /**
     * 获取电影详情
     */
    // 更简洁的 MovieController
    @GetMapping("/{movieId}")
    public ApiResponse<MovieDTO> getMovieDetail(@PathVariable Integer movieId) {
        Optional<MovieDTO> movieOptional = movieService.getMovieById(movieId);

        if (movieOptional.isPresent()) {
            return ApiResponse.success(movieOptional.get());
        } else {
            return ApiResponse.error("电影不存在");
        }
    }

    @GetMapping("/filter")
    public ApiResponse<List<MovieDTO>> filterMovies(
            @RequestParam(required = false) String genres, // 接收逗号分隔的类型字符串
            @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        Page<MovieDTO> movies = movieService.filterMovies(genres, year, page, size);
        return ApiResponse.success(movies.getContent()); // 注意：前端期望的是一个数组，所以返回 getContent()
    }

    /**

     * 提交电影评分

     */

    @PostMapping("/{movieId}/rating")

    public ApiResponse<String> submitRating(

            @PathVariable Integer movieId,

            @RequestBody Map<String, Object> body) {

        Integer userId = (Integer) body.get("userId");

        Object scoreObj = body.get("score");

        BigDecimal score;



        if (scoreObj instanceof Integer) {

            score = new BigDecimal((Integer) scoreObj);

        } else if (scoreObj instanceof Double) {

            score = BigDecimal.valueOf((Double) scoreObj);

        } else {

            return ApiResponse.error("评分格式错误");

        }



        try {

            userMovieRatingService.addOrUpdateRating(movieId, userId, score);

            return ApiResponse.success("评分提交成功");

        } catch (Exception e) {

            return ApiResponse.error("评分提交失败：" + e.getMessage());

        }

    }



    /**

     * 收藏电影

     */

    @PostMapping("/{movieId}/favorite")
    public ApiResponse<Map<String, Object>> favoriteMovie(
            @PathVariable Integer movieId,
            @RequestBody Map<String, Object> body) {
        try {
            Object userIdObj = body.get("userId");
            if (userIdObj == null) {
                return ApiResponse.error("用户ID不能为空");
            }

            Integer userId;
            // 检查并安全地转换 userId 的类型
            if (userIdObj instanceof Integer) {
                userId = (Integer) userIdObj;
            } else {
                // 如果不是 Integer，尝试从其他类型（如 Double 或 String）转换
                try {
                    // 将对象转换为字符串，然后解析为 Integer
                    userId = Integer.valueOf(String.valueOf(userIdObj));
                } catch (NumberFormatException e) {
                    // 如果转换失败，返回一个友好的错误信息
                    return ApiResponse.error("用户ID格式错误");
                }
            }

            Map<String, Object> result = userMovieFavoriteService.toggleFavorite(movieId, userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            // 捕获所有潜在的运行时异常，返回友好的错误信息
            return ApiResponse.error("操作失败：" + e.getMessage());
        }
    }



    /**

     * 分享电影（增加分享计数）

     */

    @PostMapping("/{movieId}/share")
    public ApiResponse<Map<String, Object>> shareMovie(
            @PathVariable Integer movieId,
            @RequestBody Map<String, Object> body) {
        try {
            Movie updatedMovie = movieService.incrementShareCount(movieId);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "分享成功");
            response.put("newShareCount", updatedMovie.getShareCount());
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("分享失败：" + e.getMessage());
        }
    }



    /**

     * 获取相关推荐电影

     */

    @GetMapping("/{movieId}/related")

    public ApiResponse<List<MovieDTO>> getRelatedMovies(@PathVariable Integer movieId) {

        try {

            List<MovieDTO> relatedMovies = movieService.getRelatedMovies(movieId, 6);

            return ApiResponse.success(relatedMovies);

        } catch (Exception e) {

            return ApiResponse.error("获取推荐电影失败：" + e.getMessage());

        }

    }



    /**

     * 获取电影评论

     */

    @GetMapping("/{movieId}/comments")

    public ApiResponse<List<CommentDto>> getMovieComments(@PathVariable Integer movieId) {

        try {

            return ApiResponse.success(commentService.getCommentsByMovie(movieId));

        } catch (Exception e) {

            return ApiResponse.error("获取评论失败：" + e.getMessage());

        }

    }



// 轻量搜索：搜索结果页用

    @GetMapping("/search-lite")

    public ApiResponse<Page<MovieSearchDTO>> searchMoviesLite(

            @RequestParam(name = "query", required = false) String query,

            @RequestParam(name = "keyword", required = false) String keyword,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "12") int size) {



        String finalKeyword = (query != null && !query.isEmpty()) ? query : keyword;



        Page<MovieSearchDTO> movies = movieService.searchMoviesLite(finalKeyword, page, size);

        return ApiResponse.success(movies);

    }



// 保留原来的完整搜索：详情页或需要完整信息时用

    @GetMapping("/search")

    public ApiResponse<Page<MovieDTO>> searchMovies(

            @RequestParam(name = "query", required = false) String query,

            @RequestParam(name = "keyword", required = false) String keyword,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {



        String finalKeyword = (query != null && !query.isEmpty()) ? query : keyword;



        Page<MovieDTO> movies = movieService.searchMovies(finalKeyword, page, size);

        return ApiResponse.success(movies);

    }



    /**

     * 按类型获取电影

     */

    @GetMapping("/genre/{genreName}")

    public ApiResponse<Page<MovieDTO>> getMoviesByGenre(

            @PathVariable String genreName,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        Page<MovieDTO> movies = movieService.getMoviesByGenre(genreName, page, size);

        return ApiResponse.success(movies);

    }



    /**

     * 获取所有电影（分页）

     */

    @GetMapping

    public ApiResponse<Page<MovieDTO>> getAllMovies(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        Page<MovieDTO> movies = movieService.getAllMovies(page, size);

        return ApiResponse.success(movies);

    }

}