package com.communiverse.communiverse.repo;

import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.model.like.LikeOnComment;
import com.communiverse.communiverse.model.like.LikeOnPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<LikeOnPost> findByPostIdAndUserId(Long postId, Long userId);

    Optional<LikeOnComment> findByCommentIdAndUserId(Long commentId, Long userId);

    @Query("SELECT l FROM LikeOnPost l WHERE l.user.id = :userId")
    List<LikeOnPost> findPostLikesByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM LikeOnComment l WHERE l.user.id = :userId")
    List<LikeOnComment> findCommentLikesByUserId(@Param("userId") Long userId);
}

