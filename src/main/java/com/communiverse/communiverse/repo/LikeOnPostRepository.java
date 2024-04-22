package com.communiverse.communiverse.repo;

import com.communiverse.communiverse.model.like.LikeOnPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeOnPostRepository extends JpaRepository<LikeOnPost, Long> {

    Optional<LikeOnPost> findByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT l FROM LikeOnPost l WHERE l.user.id = :userId")
    List<LikeOnPost> findPostLikesByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM LikeOnPost l WHERE l.post.id = :postId")
    List<LikeOnPost> findLikesByPostId(@Param("postId") Long postId);
}

