package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.model.like.LikeOnComment;
import com.communiverse.communiverse.model.like.LikeOnPost;
import com.communiverse.communiverse.repo.CommentRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class CommentService {

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
        return Mono.justOrEmpty(commentRepository.findById(id));
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

        // Merge the changes
        cloneComment(comment, existingComment);

        // Save the updated comment
        return Mono.just(commentRepository.save(existingComment));
    }

//    public Mono<Void> deleteComment(Long commentId) {
//
//        Mono<Comment> existingCommentMono = Mono.fromCallable(() -> commentRepository.findById(commentId))
//                .flatMap(commentOptional -> Mono.justOrEmpty(commentOptional)
//                        .switchIfEmpty(Mono.error(new RuntimeException("Comment not found " + commentId))));
//
//        // Chain the operations on the existingCommentMono Mono
//        return existingCommentMono.flatMap(existingComment -> {
//            if (existingComment == null) {
//                return Mono.error(new RuntimeException("Comment not found " + commentId));
//            }
//            // Create a Mono<Void> that completes when the delete operation is executed
//            return Mono.fromRunnable(() -> commentRepository.delete(existingComment));
//        });
//    }

    @Transactional
    public Mono<Void> deleteComment(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            Mono<User> userMono = findUserById(comment.getUser().getId());
            Mono<Post> postMono = findPostById(comment.getPost().getId());
            removeComment(comment, Objects.requireNonNull(userMono.block()), Objects.requireNonNull(postMono.block()));
        }
        return Mono.empty();
    }

    private Mono<Post> findPostById(Long postId) {
        // Create a Mono that asynchronously emits the result of calling postRepository.findById(postId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return postService.getOptionalPostMonoById(postId) // Fetch Post by ID
                .flatMap(postOptional -> Mono.justOrEmpty(postOptional) // Convert Optional to Mono
                        .switchIfEmpty(Mono.error(new RuntimeException("Post not found " + postId))));  // Throw error if Post not found
    }

    private Mono<User> findUserById(Long userId) {
        // Create a Mono that asynchronously emits the result of calling userRepository.findById(userId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return userService.getOptionalUserMonoById(userId) // Fetch User by ID
                .flatMap(userOptional -> Mono.justOrEmpty(userOptional) // Convert Optional to Mono
                        // Throw error if User not found
                        .switchIfEmpty(Mono.error(new RuntimeException("User not found " + userId))));
    }

    private void cloneComment(Comment source, Comment target) {
        target.setContent(source.getContent());
        target.setReplies(source.getReplies());
        target.setModified(LocalDateTime.now());
    }

    private void removeComment(@NotNull Comment comment, @NotNull User user, @NotNull Post post) {
        post.getComments().removeIf(c -> Objects.equals(c.getId(), comment.getId()));
        user.getComments().removeIf(c -> Objects.equals(c.getId(), comment.getId()));
        postService.updatePost(post.getId(), post);
        userService.updateUser(user.getId(), user);
        commentRepository.delete(comment); // Delete Comment in repository
    }
}

