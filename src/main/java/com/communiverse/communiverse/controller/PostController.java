package com.communiverse.communiverse.controller;

import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}/get")
    public Mono<Post> getPostById(@PathVariable("id") Long id) {
        return postService.getPostById(id);
    }

    @GetMapping
    public Flux<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Post> createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    @PutMapping("/{id}/update")
    public Mono<Post> updatePost(@PathVariable("id") Long id, @RequestBody Post post) {
        return postService.updatePost(id, post);
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deletePost(@PathVariable("id") Long id) {
        return postService.deletePost(id);
    }
}


