package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.User;
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

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> getUserById(Long id) {
         return Mono.justOrEmpty(userRepository.findById(id));
    }

    Mono<User> findUserById(Long userId) {
        // Create a Mono that asynchronously emits the result of calling userRepository.findById(userId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return getOptionalUserMonoById(userId) // Fetch User by ID
                .flatMap(userOptional -> Mono.justOrEmpty(userOptional) // Convert Optional to Mono
                        // Throw error if User not found
                        .switchIfEmpty(Mono.error(new RuntimeException("User not found " + userId))));
    }

    public @NotNull Mono<Optional<User>> getOptionalUserMonoById(Long userId) {
        return Mono.fromCallable(() -> userRepository.findByIdWithAllRelatedData(userId));
    }

    public Flux<User> getAllUsers() {
        return Flux.fromIterable(userRepository.findAll());
    }

    public Mono<User> createUser(User user) {
        return Mono.just(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Mono<User> getUserEagerlyById(Long id) {
        return getOptionalUserMonoById(id)
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

    @Transactional(readOnly = true)
    public Flux<User> getUserFollowers(Long userId) {
        Mono<Optional<User>> optionalUserMono = getOptionalUserMonoById(userId);
        return optionalUserMono.flatMapMany(optionalUser -> {
            if (optionalUser.isPresent()) {
                User followedUser = optionalUser.get();
                return Flux.fromIterable(followedUser.getFollowers());
            } else {
                return Flux.empty();
            }
        });
    }

    @Transactional
    public Mono<Void> followUser(Long userId, Long followerId) {
        User followedUser = userRepository.findByIdWithAllRelatedData(userId)
                .orElseThrow(() -> new RuntimeException("No such user with id " + userId));
        User followingUser = userRepository.findByIdWithAllRelatedData(followerId)
                .orElseThrow(() -> new RuntimeException("No such follower with id " + followerId));
        followedUser.getFollowers().add(followingUser);
        return Mono.just(userRepository.save(followedUser)).then();
    }

    @Transactional
    public Mono<Void> unfollowUser(Long userId, Long followerId) {
        User followedUser = userRepository.findByIdWithAllRelatedData(userId)
                .orElseThrow(() -> new RuntimeException("No such user with id " + userId));
        User followingUser = userRepository.findByIdWithAllRelatedData(followerId)
                .orElseThrow(() -> new RuntimeException("No such follower with id " + followerId));
        followedUser.getFollowers().removeIf(u -> u.getId().equals(followingUser.getId()));
        return Mono.just(userRepository.save(followedUser)).then();
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

