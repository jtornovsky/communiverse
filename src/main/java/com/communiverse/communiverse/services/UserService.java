package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.repo.CommentRepository;
import com.communiverse.communiverse.repo.LikeRepository;
import com.communiverse.communiverse.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserService(CommentRepository commentRepository, LikeRepository likeRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    public Mono<User> getUserById(Long id) {
         return Mono.justOrEmpty(userRepository.findById(id));
    }

    public Flux<User> getAllUsers() {
        return Flux.fromIterable(userRepository.findAll());
    }

    public Mono<User> createUser(User user) {
        return Mono.just(userRepository.save(user));
    }

    public Mono<User> getUserEagerlyById(Long id) {
        return Mono.fromCallable(() -> userRepository.findByIdWithAllRelatedData(id))
                .flatMap(optionalUser -> optionalUser.map(Mono::just).orElse(Mono.empty()));
    }

    @Transactional
    public Mono<User> updateUser(Long id, User updatedUser) {
        // Fetch the user eagerly along with nested entities
        User updatableUser = userRepository.findByIdWithAllRelatedData(id)
                .orElseThrow(() -> new RuntimeException("No such user with id " + id));

        // Merge the changes from updatedUser into updatableUser
        alterUserData(updatedUser, updatableUser);

        // Save the updated user
        return Mono.just(userRepository.save(updatableUser));
    }

    public Mono<Void> deleteUser(Long id) {
        return Mono.fromRunnable(() -> userRepository.deleteById(id));
    }

    public Flux<Comment> getUserComments(Long userId) {
        return Mono.fromCallable(() -> commentRepository.findByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Like> getUserPostLikes(Long userId) {
        return Mono.fromCallable(() -> likeRepository.findPostLikesByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Like> getUserCommentLikes(Long userId) {
        return Mono.fromCallable(() -> likeRepository.findCommentLikesByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<User> getUserFollowers(Long userId) {
        Mono<Optional<User>> optionalUserMono = Mono.fromCallable(() -> userRepository.findByIdWithFollowers(userId));
        return optionalUserMono.flatMapMany(optionalUser -> {
            if (optionalUser.isPresent()) {
                User followedUser = optionalUser.get();
                return Flux.fromIterable(followedUser.getFollowers());
            } else {
                return Flux.empty();
            }
        });
    }

    public Mono<Void> followUser(Long userId, User follower) {
        return Mono.fromCallable(() -> userRepository.findByIdWithFollowers(userId))
                .flatMap(userOptional -> {
                    if (userOptional.isPresent()) {
                        User followedUser = userOptional.get();
                        followedUser.getFollowers().add(follower);
                        return Mono.fromCallable(() -> userRepository.save(followedUser));
                    } else {
                        return Mono.error(new RuntimeException("User not found with id: " + userId));
                    }
                })
                .then();
    }

    public Mono<Void> unfollowUser(Long userId, User follower) {
        return Mono.fromCallable(() -> userRepository.findByIdWithFollowers(userId))
                .flatMap(userOptional -> {
                    if (userOptional.isPresent()) {
                        User followedUser = userOptional.get();
                        followedUser.getFollowers().removeIf(u -> u.getId().equals(follower.getId()));
                        return Mono.fromCallable(() -> userRepository.save(followedUser));
                    } else {
                        return Mono.error(new RuntimeException("User not found with id: " + userId));
                    }
                })
                .then();
    }

    @VisibleForTesting
    void alterUserData(@NotNull User source, @NotNull User target) {

        boolean isUpdated = false;

        if (source.getEmail() != null && !source.getEmail().equals(target.getEmail())) {
            target.setEmail(source.getEmail());
            isUpdated = true;
        }

        if (source.getPassword() != null && !source.getPassword().equals(target.getPassword())) {
            target.setPassword(source.getPassword());
            isUpdated = true;
        }

        if (source.getProfilePicture() != null && !source.getProfilePicture().equals(target.getProfilePicture())) {
            target.setProfilePicture(source.getProfilePicture());
            isUpdated = true;
        }

        if (source.getLastLogin() != null && !source.getLastLogin().equals(target.getLastLogin())) {
            target.setLastLogin(source.getLastLogin());
            isUpdated = true;
        }

        if (source.getComments() != null && !source.getComments().equals(target.getComments())) {
            target.setComments(source.getComments());
            isUpdated = true;
        }

        if (source.getPosts() != null && !source.getPosts().equals(target.getPosts())) {
            target.setPosts(source.getPosts());
            isUpdated = true;
        }

        if (source.getLikeOnComments() != null && !source.getLikeOnComments().equals(target.getLikeOnComments())) {
            target.setLikeOnComments(source.getLikeOnComments());
            isUpdated = true;
        }

        if (source.getLikeOnPosts() != null && !source.getLikeOnPosts().equals(target.getLikeOnPosts())) {
            target.setLikeOnPosts(source.getLikeOnPosts());
            isUpdated = true;
        }

        if (source.getFollowers() != null && !source.getFollowers().equals(target.getFollowers())) {
            target.setFollowers(source.getFollowers());
            isUpdated = true;
        }

        if (isUpdated) {
            target.setModified(LocalDateTime.now());
        }
    }
}

