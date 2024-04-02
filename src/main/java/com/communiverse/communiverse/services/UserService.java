package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Like;
import com.communiverse.communiverse.model.User;
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

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
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
        // Implement logic to fetch comments by userId from CommentRepository
        return null; // Placeholder
    }

    public Flux<Like> getUserLikes(Long userId) {
        // Implement logic to fetch likes by userId from LikeRepository
        return null; // Placeholder
    }

    public Flux<User> getUserFollowers(Long userId) {
        // Implement logic to fetch followers by userId from UserRepository
        return null; // Placeholder
    }

    public Mono<Void> followUser(Long userId, User follower) {
        // Implement logic to add follower to user's followers list in UserRepository
        return null; // Placeholder
    }

    public Mono<Void> unfollowUser(Long userId, User follower) {
        // Implement logic to remove follower from user's followers list in UserRepository
        return null; // Placeholder
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

