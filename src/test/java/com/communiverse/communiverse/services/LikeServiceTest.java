package com.communiverse.communiverse.services;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.repo.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.communiverse.communiverse.utils.CreateDataUtils.createPost;
import static com.communiverse.communiverse.utils.CreateDataUtils.createUser;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LikeServiceTest {

    private final UserService userService;
    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeOnPostRepository likeOnPostRepository;
    private final LikeOnCommentRepository likeOnCommentRepository;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @Autowired
    LikeServiceTest(UserRepository userRepository, UserService userService, CommentRepository commentRepository, CommentService commentService,
                    LikeOnCommentRepository likeOnCommentRepository, LikeOnPostRepository likeOnPostRepository,
                    PostRepository postRepository, PostService postService, LikeService likeService) {

        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
        this.likeService = likeService;

        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeOnCommentRepository = likeOnCommentRepository;
        this.likeOnPostRepository = likeOnPostRepository;
    }

    @Test
    public void testLikeUnlikePost() {

        User user1 = createUser();
        userService.createUser(user1);
        User user2 = createUser();
        userService.createUser(user2);
        User user3 = createUser();
        userService.createUser(user3);

        user1 = userService.getUserEagerlyById(user1.getId()).block();
        assert user1 != null;

        Post testedPost = createPost(user1);
        postService.createPost(testedPost);

        Mono<Like> likeMono1 = likeService.likePost(user1.getId(), testedPost.getId());
        StepVerifier.create(likeMono1)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Like> likeMono2 = likeService.likePost(user2.getId(), testedPost.getId());
        StepVerifier.create(likeMono2)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Like> likeMono3 = likeService.likePost(user3.getId(), testedPost.getId());
        StepVerifier.create(likeMono3)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Post> postMono = postService.getPostById(testedPost.getId());
        StepVerifier.create(postMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Like> postLikesMono1 = likeService.getPostLikes(testedPost.getId());
        StepVerifier.create(postLikesMono1)
                .expectNextCount(3)
                .verifyComplete();

        Flux<Like> user1LikesOnPostsMono = likeService.getUserPostLikes(user1.getId());
        StepVerifier.create(user1LikesOnPostsMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Like> user2LikesOnPostsMono = likeService.getUserPostLikes(user2.getId());
        StepVerifier.create(user2LikesOnPostsMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Like> user3LikesOnPostsMono = likeService.getUserPostLikes(user3.getId());
        StepVerifier.create(user3LikesOnPostsMono)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Void> unlikeMono1 = likeService.unlikePost(user1.getId(), testedPost.getId());
        StepVerifier.create(unlikeMono1)
                .expectNextCount(0) // No data emission expected for the Void
                .verifyComplete();

        Flux<Like> postLikesMono2 = likeService.getPostLikes(testedPost.getId());
        StepVerifier.create(postLikesMono2)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void testLikeUnlikeComment() {

        User user1 = createUser();
        userService.createUser(user1);
        User user2 = createUser();
        userService.createUser(user2);
        User user3 = createUser();
        userService.createUser(user3);

        user1 = userService.getUserEagerlyById(user1.getId()).block();

        Post testedPost = createPost(user1);
        postService.createPost(testedPost);

        Mono<Like> likeMono1 = likeService.likePost(user1.getId(), testedPost.getId());
        StepVerifier.create(likeMono1)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Like> likeMono2 = likeService.likePost(user2.getId(), testedPost.getId());
        StepVerifier.create(likeMono2)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Like> likeMono3 = likeService.likePost(user3.getId(), testedPost.getId());
        StepVerifier.create(likeMono3)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Post> postMono = postService.getPostById(testedPost.getId());
        StepVerifier.create(postMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Like> postLikesMono = likeService.getPostLikes(testedPost.getId());
        StepVerifier.create(postLikesMono)
                .expectNextCount(3)
                .verifyComplete();

        Flux<Like> user1LikesOnPostsMono = likeService.getUserPostLikes(user1.getId());
        StepVerifier.create(user1LikesOnPostsMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Like> user2LikesOnPostsMono = likeService.getUserPostLikes(user2.getId());
        StepVerifier.create(user2LikesOnPostsMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Like> user3LikesOnPostsMono = likeService.getUserPostLikes(user3.getId());
        StepVerifier.create(user3LikesOnPostsMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @BeforeEach
    void setUp() {
        setupLogger();
    }

    @AfterEach
    void cleanUp() {
        clearTestData();
    }

    private void setupLogger() {
        logger = (Logger) LoggerFactory.getLogger(UserService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    private void  clearTestData() {
        likeOnCommentRepository.deleteAllInBatch();
        likeOnPostRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
