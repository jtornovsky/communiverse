package com.communiverse.communiverse.controller;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{id}/get")
    public Mono<Comment> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id);
    }

    @GetMapping("/post/{postId}/get")
    public Flux<Comment> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.getPostComments(postId);
    }

    @GetMapping("/user/{userId}/get")
    public Flux<Comment> getCommentsByUserId(@PathVariable Long userId) {
        return commentService.getUserComments(userId);
    }

    @GetMapping("/post/{postId}/user/{userId}/get")
    public Flux<Comment> getCommentByPostIdAndUserId(@PathVariable Long postId, @PathVariable Long userId) {
        return commentService.getCommentByPostIdAndUserId(postId, userId);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Comment> createComment(@RequestBody Comment comment) {
        return commentService.createComment(comment);
    }

    @PutMapping("/{id}/update")
    public Mono<Comment> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        return commentService.updateComment(id, comment);
    }

    @DeleteMapping("/{commentId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteComment(@PathVariable Long commentId) {
        return commentService.deleteComment(commentId);
    }

    @PutMapping("/{commentId}/update")
    public Mono<Comment> updateCommentByPostIdAndUserId(@PathVariable Long commentId, @RequestBody Comment comment) {
        return commentService.updateComment(commentId, comment);
    }

    // Endpoint to get all comments by user ID
    @GetMapping("/{userId}/comments")
    public Flux<Comment> getUserComments(@PathVariable Long userId) {
        return commentService.getUserComments(userId);
    }
}

