package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.model.like.LikeOnComment;
import com.communiverse.communiverse.model.like.LikeOnPost;
import com.communiverse.communiverse.repo.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class LikeService {

    private final LikeOnCommentRepository likeOnCommentRepository;
    private final LikeOnPostRepository likeOnPostRepository;
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public LikeService(LikeOnCommentRepository likeOnCommentRepository, LikeOnPostRepository likeOnPostRepository,
            UserService userService, PostService postService, CommentService commentService) {
        this.likeOnCommentRepository = likeOnCommentRepository;
        this.likeOnPostRepository = likeOnPostRepository;
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
    }

    public Mono<Like> likePost(Long userId, Long postId) {
                // find the Post and User by their IDs
                // and combine (zip) the emissions of these two Monos into a single tuple.
        return Mono.zip(
                        findUserById(userId),
                        findPostById(postId)
                )
                // Once both Post and User are found, create a Like
                .flatMap(this::createPostLike);
    }

    public Mono<Like> likeComment(Long userId, Long commentId) {
        // find the Comment and User by their IDs
        // and combine (zip) the emissions of these two Monos into a single tuple.
        return Mono.zip(
                        findUserById(userId),
                        findCommentById(commentId)
                )
                // Once both Comment and User are found, create a Like
                .flatMap(this::createCommentLike);
    }

    public Mono<Void> unlikePost(Long userId, Long postId) {
        // Create a Mono that asynchronously emits the result of calling likeRepository.findByPostIdAndUserId(postId, userId)
        Mono<LikeOnPost> optionalLike = Mono.fromCallable(() -> likeOnPostRepository.findByPostIdAndUserId(postId, userId))
                .flatMap(Mono::justOrEmpty); // Convert Optional to Mono

        // Chain the operations on the optionalLike Mono
        return optionalLike.flatMap(like -> {
            // If the like is found, delete it
            return Mono.fromRunnable(() -> likeOnPostRepository.delete(like));
        }).then(); // Ensures that the method returns a Mono<Void>
    }

    public Mono<Void> unlikeComment(Long userId, Long commentId) {
        // Create a Mono that asynchronously emits the result of calling likeRepository.findByCommentIdAndUserId(commentId, userId)
        Mono<LikeOnComment> optionalLike = Mono.fromCallable(() -> likeOnCommentRepository.findByCommentIdAndUserId(commentId, userId))
                .flatMap(Mono::justOrEmpty); // Convert Optional to Mono

        // Chain the operations on the optionalLike Mono
        return optionalLike.flatMap(like -> {
            // If the like is found, delete it
            return Mono.fromRunnable(() -> likeOnCommentRepository.delete(like));
        }).then(); // Ensures that the method returns a Mono<Void>
    }

    private Mono<Post> findPostById(Long postId) {
        // Create a Mono that asynchronously emits the result of calling postRepository.findById(postId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return postService.getOptionalPostMonoById(postId) // Fetch Post by ID
                .flatMap(postOptional -> Mono.justOrEmpty(postOptional) // Convert Optional to Mono
                        .switchIfEmpty(Mono.error(new RuntimeException("Post not found " + postId))));  // Throw error if Post not found
    }

    private Mono<Comment> findCommentById(Long commentId) {
        // Create a Mono that asynchronously emits the result of calling commentRepository.findById(commentId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return commentService.getOptionalCommentMonoById(commentId) // Fetch Comment by ID
                .flatMap(commentOptional -> Mono.justOrEmpty(commentOptional) // Convert Optional to Mono
                        .switchIfEmpty(Mono.error(new RuntimeException("Comment not found " + commentId))));  // Throw error if Comment not found
    }

    private Mono<User> findUserById(Long userId) {
        // Create a Mono that asynchronously emits the result of calling userRepository.findById(userId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return userService.getOptionalUserMonoById(userId) // Fetch User by ID
                .flatMap(userOptional -> Mono.justOrEmpty(userOptional) // Convert Optional to Mono
                        // Throw error if User not found
                        .switchIfEmpty(Mono.error(new RuntimeException("User not found " + userId))));
    }

    public Flux<Like> getUserPostLikes(Long userId) {
        return Mono.fromCallable(() -> likeOnPostRepository.findPostLikesByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Like> getUserCommentLikes(Long userId) {
        return Mono.fromCallable(() -> likeOnCommentRepository.findCommentLikesByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    @VisibleForTesting
    Mono<LikeOnPost> createPostLike(@NotNull User user, @NotNull Post post) {
        Tuple2<User, Post> tuple = Tuples.of(user, post);
        return createPostLike(tuple);
    }

    @VisibleForTesting
    Mono<LikeOnComment> createCommentLike(@NotNull User user, @NotNull Comment comment) {
        Tuple2<User, Comment> tuple = Tuples.of(user, comment);
        return createCommentLike(tuple);
    }

    @VisibleForTesting
    Mono<LikeOnPost> createPostLike(@NotNull LikeOnPost like) {
        return Mono.just(likeOnPostRepository.save(like));
    }

    @VisibleForTesting
    Mono<LikeOnComment> createCommentLike(@NotNull LikeOnComment like) {
        return Mono.just(likeOnCommentRepository.save(like));
    }

    private @NotNull Mono<LikeOnPost> createPostLike(@NotNull Tuple2<User, Post> tuple) {
        User user = tuple.getT1(); // Get User from tuple
        Post post = tuple.getT2(); // Get Post from tuple
        LikeOnPost like = new LikeOnPost();    // Create new Like
        like.setPost(post);        // Set Post in Like
        like.setUser(user);        // Set User in Like
        post.getLikes().add(like);
        return Mono.just(likeOnPostRepository.save(like)); // Save Like to repository
    }

    private @NotNull Mono<LikeOnComment> createCommentLike(@NotNull Tuple2<User, Comment> tuple) {
        User user = tuple.getT1(); // Get User from tuple
        Comment comment = tuple.getT2(); // Get Comment from tuple
        LikeOnComment like = new LikeOnComment();    // Create new Like
        like.setComment(comment);        // Set Comment in Like
        like.setUser(user);        // Set User in Like
        comment.getLikes().add(like);
        return Mono.just(likeOnCommentRepository.save(like)); // Save Like to repository
    }
}

