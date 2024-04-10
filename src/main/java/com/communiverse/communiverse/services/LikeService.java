package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.model.like.LikeOnComment;
import com.communiverse.communiverse.model.like.LikeOnPost;
import com.communiverse.communiverse.repo.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class LikeService {

    private final LikeOnCommentRepository likeOnCommentRepository;
    private final LikeOnPostRepository likeOnPostRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public LikeService(LikeOnCommentRepository likeOnCommentRepository, LikeOnPostRepository likeOnPostRepository,
                       PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.likeOnCommentRepository = likeOnCommentRepository;
        this.likeOnPostRepository = likeOnPostRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public Mono<Like> likePost(Long postId, Long userId) {
                // find the Post and User by their IDs
                // and combine (zip) the emissions of these two Monos into a single tuple.
        return Mono.zip(
                        findPostById(postId),
                        findUserById(userId)
                )
                // Once both Post and User are found, create a Like
                .flatMap(this::createPostLike);
    }

    public Mono<Like> likeComment(Long commentId, Long userId) {
        // find the Comment and User by their IDs
        // and combine (zip) the emissions of these two Monos into a single tuple.
        return Mono.zip(
                        findCommentById(commentId),
                        findUserById(userId)
                )
                // Once both Comment and User are found, create a Like
                .flatMap(this::createCommentLike);
    }

    public Mono<Void> unlikePost(Long postId, Long userId) {
        // Create a Mono that asynchronously emits the result of calling likeRepository.findByPostIdAndUserId(postId, userId)
        Mono<LikeOnPost> optionalLike = Mono.fromCallable(() -> likeOnPostRepository.findByPostIdAndUserId(postId, userId))
                .flatMap(Mono::justOrEmpty); // Convert Optional to Mono

        // Chain the operations on the optionalLike Mono
        return optionalLike.flatMap(like -> {
            // If the like is found, delete it
            return Mono.fromRunnable(() -> likeOnPostRepository.delete(like));
        }).then(); // Ensures that the method returns a Mono<Void>
    }

    public Mono<Void> unlikeComment(Long commentId, Long userId) {
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
        return Mono.fromCallable(() -> postRepository.findById(postId)) // Fetch Post by ID
                .flatMap(postOptional -> Mono.justOrEmpty(postOptional) // Convert Optional to Mono
                        .switchIfEmpty(Mono.error(new RuntimeException("Post not found " + postId))));  // Throw error if Post not found
    }

    private Mono<Comment> findCommentById(Long commentId) {
        // Create a Mono that asynchronously emits the result of calling commentRepository.findById(commentId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return Mono.fromCallable(() -> commentRepository.findById(commentId)) // Fetch Comment by ID
                .flatMap(commentOptional -> Mono.justOrEmpty(commentOptional) // Convert Optional to Mono
                        .switchIfEmpty(Mono.error(new RuntimeException("Comment not found " + commentId))));  // Throw error if Comment not found
    }

    private Mono<User> findUserById(Long userId) {
        // Create a Mono that asynchronously emits the result of calling userRepository.findById(userId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return Mono.fromCallable(() -> userRepository.findById(userId)) // Fetch User by ID
                .flatMap(userOptional -> Mono.justOrEmpty(userOptional) // Convert Optional to Mono
                        // Throw error if User not found
                        .switchIfEmpty(Mono.error(new RuntimeException("User not found " + userId))));
    }

    private @NotNull Mono<LikeOnPost> createPostLike(@NotNull Tuple2<Post, User> tuple) {
        Post post = tuple.getT1(); // Get Post from tuple
        User user = tuple.getT2(); // Get User from tuple
        LikeOnPost like = new LikeOnPost();    // Create new Like
        like.setPost(post);        // Set Post in Like
        like.setUser(user);        // Set User in Like
        post.getLikes().add(like);
        return Mono.fromCallable(() -> likeOnPostRepository.save(like)); // Save Like to repository
    }

    private @NotNull Mono<LikeOnComment> createCommentLike(@NotNull Tuple2<Comment, User> tuple) {
        Comment comment = tuple.getT1(); // Get Comment from tuple
        User user = tuple.getT2(); // Get User from tuple
        LikeOnComment like = new LikeOnComment();    // Create new Like
        like.setComment(comment);        // Set Post in Like
        like.setUser(user);        // Set User in Like
        comment.getLikes().add(like);
        return Mono.fromCallable(() -> likeOnCommentRepository.save(like)); // Save Like to repository
    }
}

