package com.communiverse.communiverse.controller;

import com.communiverse.communiverse.model.Like;
import com.communiverse.communiverse.services.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
        return likeService.likePost(postId, userId);
    }

    @DeleteMapping("/{postId}/{userId}/unlike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unlikePost(@PathVariable Long postId, @PathVariable Long userId) {
        return likeService.unlikePost(postId, userId);
    }

    @DeleteMapping("/{likeId}/unlike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unlikePost(@PathVariable Long likeId) {
        return likeService.unlikePost(likeId);
    }
}

