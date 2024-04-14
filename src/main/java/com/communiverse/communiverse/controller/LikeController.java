package com.communiverse.communiverse.controller;

import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.services.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{postId}/{userId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Like> likePost(@PathVariable Long postId, @PathVariable Long userId) {
        return likeService.likePost(userId, postId);
    }

    @DeleteMapping("/{postId}/{userId}/unlike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unlikePost(@PathVariable Long postId, @PathVariable Long userId) {
        return likeService.unlikePost(userId, postId);
    }

    @PostMapping("/{commentId}/{userId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Like> likeComment(@PathVariable Long commentId, @PathVariable Long userId) {
        return likeService.likeComment(userId, commentId);
    }

    @DeleteMapping("/{commentId}/{userId}/unlike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unlikeComment(@PathVariable Long commentId, @PathVariable Long userId) {
        return likeService.unlikeComment(userId, commentId);
    }


    // Endpoint to get all posts likes by user ID
    @GetMapping("/{userId}/post-likes")
    public Flux<Like> getUserPostLikes(@PathVariable Long userId) {
        return likeService.getUserPostLikes(userId);
    }

    // Endpoint to get all comments likes by user ID
    @GetMapping("/{userId}/comment-likes")
    public Flux<Like> getUserCommentLikes(@PathVariable Long userId) {
        return likeService.getUserCommentLikes(userId);
    }
}

