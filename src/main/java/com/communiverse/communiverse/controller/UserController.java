package com.communiverse.communiverse.controller;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Like;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    // Endpoint to get all comments by user ID
    @GetMapping("/{userId}/comments")
    public Flux<Comment> getUserComments(@PathVariable Long userId) {
        return userService.getUserComments(userId);
    }

    // Endpoint to get all likes by user ID
    @GetMapping("/{userId}/likes")
    public Flux<Like> getUserLikes(@PathVariable Long userId) {
        return userService.getUserLikes(userId);
    }

    // Endpoint to get all followers by user ID
    @GetMapping("/{userId}/followers")
    public Flux<User> getUserFollowers(@PathVariable Long userId) {
        return userService.getUserFollowers(userId);
    }

    // Endpoint to follow a user
    @PostMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> followUser(@PathVariable Long userId, @RequestBody User follower) {
        return userService.followUser(userId, follower);
    }

    // Endpoint to unfollow a user
    @DeleteMapping("/{userId}/unfollow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unfollowUser(@PathVariable Long userId, @RequestBody User follower) {
        return userService.unfollowUser(userId, follower);
    }
}

