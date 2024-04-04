package com.communiverse.communiverse.repo;

import com.communiverse.communiverse.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    Optional<Like> findByCommentIdAndUserId(Long commentId, Long userId);

    List<Like> findByUserId(Long userId);
}

