package com.communiverse.communiverse.repo;

import com.communiverse.communiverse.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostId(Long postId);

    List<Comment> findByUserId(Long userId);

    List<Comment> findByPostIdAndUserId(Long postId, Long userId);

//    @Query("SELECT c FROM Comment c " +
//            "LEFT JOIN FETCH c.likes " +
//            "WHERE c.id = :commentId")
//    Optional<Comment> findByIdWithAllRelatedData(Long commentId);
}
