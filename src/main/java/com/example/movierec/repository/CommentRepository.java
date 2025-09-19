// src/main/java/com/example/movierec/repository/CommentRepository.java

package com.example.movierec.repository;

import com.example.movierec.entity.Comment;
import com.example.movierec.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // 获取某个电影下的所有顶级评论（父评论为空），按创建时间排序
    List<Comment> findByMovieAndParentIsNullOrderByCreatedAtAsc(Movie movie);
    /**
     * 根据用户ID查询其发表的所有评论，并按创建时间倒序排列。
     * 倒序排列可以使得最新评论显示在列表顶部。
     */
    List<Comment> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    List<Comment> findByUserUserId(Integer userId);
    List<Comment> findByParentCommentIdInOrderByCreatedAtDesc(List<Integer> parentIds);
    List<Comment> findByUserUserIdAndParentIsNullOrderByCreatedAtDesc(Integer userId);
}