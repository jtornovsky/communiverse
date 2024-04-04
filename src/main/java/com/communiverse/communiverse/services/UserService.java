package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Like;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.repo.CommentRepository;
import com.communiverse.communiverse.repo.LikeRepository;
import com.communiverse.communiverse.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public Mono<User> updateUser(Long id, User user) {
        // Create a Mono that asynchronously fetches the Optional<User> from the repository
        Mono<Optional<User>> optionalUserMono = Mono.fromCallable(() -> userRepository.findById(id));

        // Process the optional user inside the flatMap
        return optionalUserMono.flatMap(optionalUser -> {
            if (optionalUser.isPresent()) {
                // If the Optional<User> contains a user, update it and save
                User existingUser = optionalUser.get();
                cloneUser(user, existingUser);
                return Mono.just(userRepository.save(existingUser));
            } else {
                // If the Optional<User> is empty, log a warning and throw an exception
                log.warn("No such user with id {}", id);
                throw new RuntimeException("No such user with id " + id);
            }
        });
    }

    public Mono<Void> deleteUser(Long id) {
        return Mono.fromRunnable(() -> userRepository.deleteById(id));
    }

    public Flux<Comment> getUserComments(Long userId) {
        return Mono.fromCallable(() -> commentRepository.findByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Like> getUserLikes(Long userId) {
        return Mono.fromCallable(() -> likeRepository.findByUserId(userId))
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

    private void cloneUser(User source, User target) {
        target.setEmail(source.getEmail());
        target.setPassword(source.getPassword());
        target.setProfilePicture(source.getProfilePicture());
        target.setLastLogin(source.getLastLogin());
        target.setComments(source.getComments());
        target.setPosts(source.getPosts());
        target.setModified(LocalDateTime.now());
    }
}

