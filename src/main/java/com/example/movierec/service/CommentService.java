// src/main/java/com/example/movierec/service/CommentService.java

package com.example.movierec.service;

import com.example.movierec.dto.CommentDto;
import com.example.movierec.entity.Comment;
import com.example.movierec.entity.Movie;
import com.example.movierec.entity.User;
import com.example.movierec.exception.ResourceNotFoundException;
import com.example.movierec.repository.CommentRepository;
import com.example.movierec.repository.MovieRepository;
import com.example.movierec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 根据电影ID获取评论列表，并按时间排序。
     * @param movieId 电影ID
     * @return 包含评论树的 DTO 列表
     */
    public List<CommentDto> getCommentsByMovie(Integer movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("电影不存在: " + movieId));

        List<Comment> topLevelComments = commentRepository.findByMovieAndParentIsNullOrderByCreatedAtAsc(movie);

        return topLevelComments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 添加新评论或回复。
     * @return 新评论的 DTO 对象
     */
    @Transactional
    public CommentDto addComment(Integer movieId, Integer userId, String content, Integer parentId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("电影不存在: " + movieId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        Comment comment = new Comment();
        comment.setMovie(movie);
        comment.setUser(user);
        comment.setContent(content);
        comment.setLikes(0);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("父评论不存在: " + parentId));
            comment.setParent(parent);
        }

        Comment saved = commentRepository.save(comment);

        return convertToDto(saved);
    }

    /**
     * 对评论点赞。
     * @return 更新后的评论 DTO 对象
     */
    @Transactional
    public List<CommentDto> likeComment(Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        // 增加点赞数
        int currentLikes = Optional.ofNullable(comment.getLikes()).orElse(0);
        comment.setLikes(currentLikes + 1);

        // 保存更改
        commentRepository.save(comment);

        // 找到这个评论的顶级父评论
        Comment rootComment = comment;
        while (rootComment.getParent() != null) {
            rootComment = rootComment.getParent();
        }

        // 获取这个顶级评论所属的电影ID
        Integer movieId = rootComment.getMovie().getMovieId();

        // 调用 getCommentsByMovie 方法，获取并返回包含所有最新点赞数的完整评论列表
        return getCommentsByMovie(movieId);
    }

    /**
     * 获取用户的所有评论和回复。
     * @param userId 用户ID
     * @return 评论的 DTO 列表，包含电影标题。
     */
    public List<CommentDto> getCommentsByUser(Integer userId) {
        // 确保用户存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在: " + userId);
        }

        // 查找所有该用户发表的评论
        // 注意：此方法需要在 CommentRepository 中定义
        List<Comment> userComments = commentRepository.findByUserUserIdOrderByCreatedAtDesc(userId);

        // 将实体列表转换为 DTO 列表
        return userComments.stream()
                .map(this::convertToDtoWithMovieTitle)
                .collect(Collectors.toList());
    }

    /**
     * 获取回复给指定用户的所有评论。
     * @param userId 用户ID
     * @return 包含回复信息的 CommentDto 列表
     */
    public List<CommentDto> getRepliesToUserComments(Integer userId) {
        // 1. 查找该用户发表过的所有评论 ID
        List<Integer> userCommentIds = commentRepository.findByUserUserId(userId).stream()
                .map(Comment::getCommentId)
                .collect(Collectors.toList());

        if (userCommentIds.isEmpty()) {
            return new ArrayList<>(); // 如果用户没有发表过任何评论，则直接返回空列表
        }

        // 2. 查找所有 parentId 在 userCommentIds 列表中的评论（即所有回复）
        List<Comment> replies = commentRepository.findByParentCommentIdInOrderByCreatedAtDesc(userCommentIds);

        // 3. 将结果转换为 DTO
        return replies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户自己发表的所有评论及其回复。
     * @param userId 用户ID
     * @return 包含评论及其回复的 CommentDto 列表
     */
    public List<CommentDto> getMyCommentsWithReplies(Integer userId) {
        // 1. 查找该用户发表的所有顶级评论（parent_id为null）
        List<Comment> myTopLevelComments = commentRepository.findByUserUserIdAndParentIsNullOrderByCreatedAtDesc(userId);

        // 2. 将这些评论转换为 CommentDto，并递归获取其回复
        return myTopLevelComments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 将 Comment 实体转换为 CommentDto，并包含电影标题和电影ID。
     */
    private CommentDto convertToDtoWithMovieTitle(Comment comment) {
        CommentDto dto = convertToDto(comment);
        // 添加电影标题和电影ID，前端需要这些信息来显示和跳转
        if (comment.getMovie() != null) {
            dto.setMovieTitle(comment.getMovie().getTitle());
            dto.setMovieId(comment.getMovie().getMovieId()); // 新增：设置电影ID
        }
        return dto;
    }

    /**
     * 将 Comment 实体转换为 CommentDto，并递归处理嵌套的回复。
     */
    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getCommentId());
        dto.setUserId(comment.getUser().getUserId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setContent(comment.getContent());
        dto.setLikes(comment.getLikes());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getMovie() != null) {
            dto.setMovieId(comment.getMovie().getMovieId());
            dto.setMovieTitle(comment.getMovie().getTitle());
        }

        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getCommentId());
        }

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setReplies(new ArrayList<>());
        }
        return dto;
    }
}