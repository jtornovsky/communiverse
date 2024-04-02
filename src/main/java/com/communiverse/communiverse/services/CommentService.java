package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.repo.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Mono<Comment> getCommentById(Long id) {
        return Mono.justOrEmpty(commentRepository.findById(id));
    }

    public Flux<Comment> getCommentsByPostId(Long postId) {
        return Flux.fromIterable(commentRepository.findByPostId(postId));
    }

    public Flux<Comment> getCommentsByUserId(Long userId) {
        return Flux.fromIterable(commentRepository.findByUserId(userId));
    }

    public Mono<Comment> getCommentByPostIdAndUserId(Long postId, Long userId) {
        return Mono.justOrEmpty(commentRepository.findByPostIdAndUserId(postId, userId));
    }

    public Mono<Comment> createComment(Comment comment) {
        return Mono.just(commentRepository.save(comment));
    }

    public Mono<Comment> updateComment(Long id, Comment comment) {

        Mono<Optional<Comment>> optionalCommentMono = Mono.fromCallable(() -> commentRepository.findById(id));

        return optionalCommentMono.flatMap(optionalComment -> {
            if (optionalComment.isEmpty()) {
                return Mono.error(new RuntimeException("No such comment with id " + id));
            }

            Comment existingComment = optionalComment.get();
            cloneComment(comment, existingComment);
            return Mono.fromCallable(() -> commentRepository.save(existingComment));
        });
    }

    public Mono<Comment> updateCommentByPostIdAndUserId(Long postId, Long userId, Comment comment) {
        // Create a Mono that asynchronously emits the result of calling commentRepository.findByPostIdAndUserId(postId, userId)
        Mono<Comment> existingCommentMono = Mono.fromCallable(() -> commentRepository.findByPostIdAndUserId(postId, userId));

        // Chain the operations on the existingCommentMono Mono
        return existingCommentMono.flatMap(existingComment -> {
            if (existingComment == null) {
                return Mono.error(new RuntimeException("No such comment for postId " + postId + " and userId " + userId));
            }
            cloneComment(comment, existingComment);
            return Mono.fromCallable(() -> commentRepository.save(existingComment));
        });
    }

    public Mono<Void> deleteComment(Long id) {
        return Mono.fromRunnable(() -> commentRepository.deleteById(id));
    }

    public Mono<Void> deleteCommentByPostIdAndUserId(Long postId, Long userId) {
        // Create a Mono that asynchronously emits the result of calling commentRepository.findByPostIdAndUserId(postId, userId)
        Mono<Comment> existingCommentMono = Mono.fromCallable(() -> commentRepository.findByPostIdAndUserId(postId, userId));

        // Chain the operations on the existingCommentMono Mono
        return existingCommentMono.flatMap(existingComment -> {
            if (existingComment == null) {
                return Mono.error(new RuntimeException("No such comment for postId " + postId + " and userId " + userId));
            }
            // Create a Mono<Void> that completes when the delete operation is executed
            return Mono.fromRunnable(() -> commentRepository.delete(existingComment));
        });
    }

    private void cloneComment(Comment source, Comment target) {
        target.setContent(source.getContent());
        target.setReplies(source.getReplies());
        target.setModified(LocalDateTime.now());
    }
}

