package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Like;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.repo.LikeRepository;
import com.communiverse.communiverse.repo.PostRepository;
import com.communiverse.communiverse.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository, PostRepository postRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
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
                .flatMap(this::createLike);
    }

    public Mono<Void> unlikePost(Long postId, Long userId) {
        // Create a Mono that asynchronously emits the result of calling likeRepository.findByPostIdAndUserId(postId, userId)
        Mono<Like> optionalLike = Mono.fromCallable(() -> likeRepository.findByPostIdAndUserId(postId, userId))
                .flatMap(Mono::justOrEmpty); // Convert Optional to Mono

        // Chain the operations on the optionalLike Mono
        return optionalLike.flatMap(like -> {
            // If the like is found, delete it
            return Mono.fromRunnable(() -> likeRepository.delete(like));
        }).then(); // Ensures that the method returns a Mono<Void>
    }

    public Mono<Void> unlikePost(Long likeId) {
        /*
            use Mono.fromRunnable to create a Mono that completes once the deleteById operation is executed.
            This way, we ensure that the unlikePost method returns a Mono<Void>, as expected
         */
        return Mono.fromRunnable(() -> likeRepository.deleteById(likeId));
    }

    private Mono<Post> findPostById(Long postId) {
        // Create a Mono that asynchronously emits the result of calling postRepository.findById(postId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return Mono.fromCallable(() -> postRepository.findById(postId)) // Fetch Post by ID
                .flatMap(postOptional -> Mono.justOrEmpty(postOptional) // Convert Optional to Mono
                        .switchIfEmpty(Mono.error(new RuntimeException("Post not found " + postId))));  // Throw error if Post not found
    }

    private Mono<User> findUserById(Long userId) {
        // Create a Mono that asynchronously emits the result of calling userRepository.findById(userId)
        // The result is obtained by calling the method in a Callable, which allows for lazy evaluation
        return Mono.fromCallable(() -> userRepository.findById(userId)) // Fetch User by ID
                .flatMap(userOptional -> Mono.justOrEmpty(userOptional) // Convert Optional to Mono
                        // Throw error if User not found
                        .switchIfEmpty(Mono.error(new RuntimeException("User not found " + userId))));
    }

    private Mono<Like> createLike(Tuple2<Post, User> tuple) {
        Post post = tuple.getT1(); // Get Post from tuple
        User user = tuple.getT2(); // Get User from tuple
        Like like = new Like();    // Create new Like
        like.setPost(post);        // Set Post in Like
        like.setUser(user);        // Set User in Like
        return Mono.fromCallable(() -> likeRepository.save(like)); // Save Like to repository
    }
}

