package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.model.like.LikeOnComment;
import com.communiverse.communiverse.model.like.LikeOnPost;
import com.communiverse.communiverse.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
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
                        userService.findUserById(userId),
                        postService.findPostById(postId)
                )
                // Once both Post and User are found, create a Like
                .flatMap(this::createPostLike);
    }

    public Mono<Like> likeComment(Long userId, Long commentId) {
        // find the Comment and User by their IDs
        // and combine (zip) the emissions of these two Monos into a single tuple.
        return Mono.zip(
                        userService.findUserById(userId),
                        commentService.findCommentById(commentId)
                )
                // Once both Comment and User are found, create a Like
                .flatMap(this::createCommentLike);
    }

    // todo: need to implement this method in reactive approach
    //  currently reactive approach doesn't delete like from the DB for unclear reason
    @Transactional
    public Mono<Void> unlikePost(Long userId, Long postId) {
        Optional<LikeOnPost> optionalLike = likeOnPostRepository.findByPostIdAndUserId(postId, userId);
        if (optionalLike.isPresent()) {
            Mono<User> userMono = userService.findUserById(userId);
            Mono<Post> postMono = postService.findPostById(postId);
            deletePostLike(optionalLike.get(), Objects.requireNonNull(userMono.block()), Objects.requireNonNull(postMono.block()));
        }
        return Mono.empty();
    }

    // todo: need to implement this method in reactive approach
    //  currently reactive approach doesn't delete like from the DB for unclear reason
    @Transactional
    public Mono<Void> unlikeComment(Long userId, Long commentId) {
        Optional<LikeOnComment> optionalLike = likeOnCommentRepository.findByCommentIdAndUserId(commentId, userId);
        if (optionalLike.isPresent()) {
            Mono<User> userMono = userService.findUserById(userId);
            Mono<Comment> commentMono = commentService.findCommentById(commentId);
            deleteCommentLike(optionalLike.get(), Objects.requireNonNull(userMono.block()), Objects.requireNonNull(commentMono.block()));
        }
        return Mono.empty();
    }

    public Flux<Like> getUserPostLikes(Long userId) {
        return Mono.fromCallable(() -> likeOnPostRepository.findPostLikesByUserId(userId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Like> getPostLikes(Long postId) {
        return Mono.fromCallable(() -> likeOnPostRepository.findLikesByPostId(postId))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Like> getCommentLikes(Long commentId) {
        return Mono.fromCallable(() -> likeOnCommentRepository.findLikesByCommentId(commentId))
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
        return Mono.just(likeOnPostRepository.save(like)); // Save Like to repository
    }

    private void deletePostLike(@NotNull LikeOnPost like, @NotNull User user, @NotNull Post post) {
        post.getLikes().removeIf(l -> Objects.equals(l.getId(), like.getId()));
        user.getLikeOnPosts().removeIf(l -> Objects.equals(l.getId(), like.getId()));
        postService.updatePost(post.getId(), post);
        userService.updateUser(user.getId(), user);
        likeOnPostRepository.delete(like); // Delete Like in repository
    }

    private @NotNull Mono<LikeOnComment> createCommentLike(@NotNull Tuple2<User, Comment> tuple) {
        User user = tuple.getT1(); // Get User from tuple
        Comment comment = tuple.getT2(); // Get Comment from tuple
        LikeOnComment like = new LikeOnComment();    // Create new Like
        like.setComment(comment);        // Set Comment in Like
        like.setUser(user);        // Set User in Like
        return Mono.just(likeOnCommentRepository.save(like)); // Save Like to repository
    }

    private void deleteCommentLike(@NotNull LikeOnComment like, @NotNull User user, @NotNull Comment comment) {
        comment.getLikes().removeIf(l -> Objects.equals(l.getId(), like.getId()));
        user.getLikeOnComments().removeIf(l -> Objects.equals(l.getId(), like.getId()));
        commentService.updateComment(comment.getId(), comment);
        userService.updateUser(user.getId(), user);
        likeOnCommentRepository.delete(like); // Delete Like in repository
    }
}

