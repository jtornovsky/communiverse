package com.communiverse.communiverse.repo;

import com.communiverse.communiverse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.followers WHERE u.id = :userId")
    Optional<User> findByIdWithFollowers(Long userId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.followers " +
            "LEFT JOIN FETCH u.likeOnPosts " +
            "LEFT JOIN FETCH u.likeOnComments " +
            "LEFT JOIN FETCH u.comments " +
            "LEFT JOIN FETCH u.posts " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithAllRelatedData(Long userId);
}
