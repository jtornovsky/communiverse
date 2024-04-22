package com.communiverse.communiverse.repo;

import com.communiverse.communiverse.model.like.LikeOnComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeOnCommentRepository extends JpaRepository<LikeOnComment, Long> {

    Optional<LikeOnComment> findByCommentIdAndUserId(Long commentId, Long userId);

    @Query("SELECT l FROM LikeOnComment l WHERE l.user.id = :userId")
    List<LikeOnComment> findCommentLikesByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM LikeOnComment l WHERE l.comment.id = :commentId")
    List<LikeOnComment> findLikesByCommentId(@Param("commentId") Long commentId);
}

