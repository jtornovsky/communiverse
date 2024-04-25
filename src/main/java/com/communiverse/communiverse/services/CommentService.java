package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.repo.CommentRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final String DELETED_COMMENT = "Comment deleted";

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostService postService;

    @Autowired
    public CommentService(CommentRepository commentRepository, UserService userService, PostService postService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.postService = postService;
    }

    public Mono<Comment> getCommentById(Long id) {
        return findCommentById(id);
    }

    public Mono<Comment> findCommentById(Long commentId) {
        // Create a Mono that asynchronously emits the result of calling commentRepository.findById(commentId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return getOptionalCommentMonoById(commentId) // Fetch Comment by ID
                .flatMap(commentOptional -> Mono.justOrEmpty(commentOptional) // Convert Optional to Mono
                        .switchIfEmpty(Mono.error(new RuntimeException("Comment not found " + commentId))));  // Throw error if Comment not found
    }

    public @NotNull Mono<Optional<Comment>> getOptionalCommentMonoById(Long userId) {
        return Mono.fromCallable(() -> commentRepository.findById(userId));
    }

    public Flux<Comment> getPostComments(Long postId) {
        return Mono.fromCallable(() -> commentRepository.findByPostId(postId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Comment> getUserComments(Long userId) {
        return Mono.fromCallable(() -> commentRepository.findByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Comment> getCommentByPostIdAndUserId(Long postId, Long userId) {
        return Mono.fromCallable(() -> commentRepository.findByPostIdAndUserId(postId, userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Comment> createComment(Comment comment) {
        return Mono.just(commentRepository.save(comment));
    }

    @Transactional
    public Mono<Comment> updateComment(Long id, Comment comment) {

        // Fetch the comment eagerly along with nested entities
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No such comment with id " + id));

        if (existingComment.getContent().equalsIgnoreCase(DELETED_COMMENT)) {
            log.warn("The comment with id {} marked as 'deleted' and cannot be updated", id);
            return Mono.empty();
        }

        // Merge the changes
        cloneComment(comment, existingComment);

        // Save the updated comment
        return Mono.just(commentRepository.save(existingComment));
    }

    @Transactional
    public Mono<Void> deleteComment(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            Mono<User> userMono = userService.findUserById(comment.getUser().getId());
            Mono<Post> postMono = postService.findPostById(comment.getPost().getId());
            removeComment(comment, Objects.requireNonNull(userMono.block()), Objects.requireNonNull(postMono.block()));
        }
        return Mono.empty();
    }

    private void cloneComment(Comment source, Comment target) {
        target.setContent(source.getContent());
        target.setReplies(source.getReplies());
        target.setModified(LocalDateTime.now());
    }

    private void removeComment(@NotNull Comment comment, @NotNull User user, @NotNull Post post) {

        if (!CollectionUtils.isEmpty(comment.getReplies())) {
            log.warn("Comment with id {} has replies, so just marking it as 'deleted'", comment.getId());
            comment.setContent(DELETED_COMMENT);
            updateComment(comment.getId(), comment);
            return;
        }

        Comment parentComment = comment.getParentComment();
        if (parentComment != null) {
            parentComment.getReplies().removeIf(c -> Objects.equals(c.getId(), comment.getId()));
        }

        post.getComments().removeIf(c -> Objects.equals(c.getId(), comment.getId()));
        user.getComments().removeIf(c -> Objects.equals(c.getId(), comment.getId()));
        postService.updatePost(post.getId(), post);
        userService.updateUser(user.getId(), user);
        commentRepository.delete(comment); // Delete Comment in repository
        return;
    }
}

