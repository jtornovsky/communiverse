package com.communiverse.communiverse.services;

import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.repo.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Mono<Post> getPostById(Long id) {
        return Mono.justOrEmpty(postRepository.findById(id));
    }

    public Flux<Post> getAllPosts() {
        return Flux.fromIterable(postRepository.findAll());
    }

    public Mono<Post> createPost(Post post) {
        // Save the post and return it wrapped in a Mono
        return Mono.just(postRepository.save(post));
    }

    public Mono<Post> updatePost(Long id, Post post) {
        // Create a Mono that asynchronously fetches the Optional<Post> from the repository
        Mono<Optional<Post>> optionalPostMono = Mono.fromCallable(() -> postRepository.findById(id));

        // Process the optional post inside the flatMap
        return optionalPostMono.flatMap(optionalPost -> {
            if (optionalPost.isPresent()) {
                // If the Optional<Post> contains a post, update it and save
                Post existingPost = optionalPost.get();
                clonePost(post, existingPost);
                return Mono.just(postRepository.save(existingPost));
            } else {
                // If the Optional<Post> is empty, log a warning and throw an exception
                throw new RuntimeException("No such post with id " + id);
            }
        });
    }

    public Mono<Void> deletePost(Long id) {
        /*
            use Mono.fromRunnable to create a Mono that completes once the deleteById operation is executed.
            This way, we ensure that the deletePost method returns a Mono<Void>, as expected
         */
        return Mono.fromRunnable(() -> postRepository.deleteById(id));
    }

    private void clonePost(Post source, Post target) {
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setComments(source.getComments());
        target.setImage(source.getImage());
        target.setModified(LocalDateTime.now());
    }
}
