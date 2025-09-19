package com.example.movierec.controller;

import com.example.movierec.dto.ApiResponse;
import com.example.movierec.dto.CommentDto;
import com.example.movierec.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.movierec.dto.CommentRequestDto;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // <-- 修改这里
@CrossOrigin(origins = "http://localhost:8081")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/comments/movie/{movieId}") // <-- 修改这里
    public ApiResponse<List<CommentDto>> getComments(@PathVariable Integer movieId) {
        return ApiResponse.success(commentService.getCommentsByMovie(movieId));
    }

    @PostMapping("/comments") // <-- 修改这里
    public ApiResponse<CommentDto> addComment(@RequestBody CommentRequestDto request) {
        // 现在你可以安全地从请求对象中获取数据
        return ApiResponse.success(commentService.addComment(
                request.getMovieId(),
                request.getUserId(),
                request.getContent(),
                request.getParentId()
        ));
    }
    /**
     * 根据用户ID获取其发表的所有评论和回复
     * @param userId 用户ID
     * @return 该用户发表的评论列表
     */
    @GetMapping("/users/{userId}/comments") // <-- 修改这里
    public ApiResponse<List<CommentDto>> getCommentsByUser(@PathVariable Integer userId) {
        return ApiResponse.success(commentService.getCommentsByUser(userId));
    }

    @PostMapping("/comments/{commentId}/like") // <-- 修改这里
    public ResponseEntity<List<CommentDto>> likeComment(@PathVariable Integer commentId) {
        // 直接调用只包含 commentId 的方法
        List<CommentDto> updatedComments = commentService.likeComment(commentId);
        return ResponseEntity.ok(updatedComments);
    }
    /**
     * 获取回复给指定用户的评论
     * @param userId 用户ID
     * @return 回复列表
     */
    @GetMapping("/comments/replies-to/{userId}")
    public ApiResponse<List<CommentDto>> getRepliesToUser(@PathVariable Integer userId) {
        List<CommentDto> replies = commentService.getRepliesToUserComments(userId);
        return ApiResponse.success(replies);
    }
    /**
     * 获取用户自己发表的所有评论及其回复
     * @param userId 用户ID
     * @return 包含评论及其回复的列表
     */
    @GetMapping("/comments/my-comments-with-replies/{userId}")
    public ApiResponse<List<CommentDto>> getMyCommentsWithReplies(@PathVariable Integer userId) {
        List<CommentDto> comments = commentService.getMyCommentsWithReplies(userId);
        return ApiResponse.success(comments);
    }
}