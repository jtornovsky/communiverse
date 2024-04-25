package com.communiverse.communiverse.services;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.communiverse.communiverse.model.Comment;
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

import java.util.Set;

import static com.communiverse.communiverse.utils.CreateDataUtils.*;
import static com.communiverse.communiverse.utils.CreateDataUtils.createCommentReply;
import static com.communiverse.communiverse.utils.VerificationResultsUtils.verifyCommentFields;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentServiceTest {

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
    CommentServiceTest(UserRepository userRepository, UserService userService, CommentRepository commentRepository, CommentService commentService,
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

    @BeforeEach
    void setUp() {
        setupLogger();
    }

    @AfterEach
    void cleanUp() {
        clearTestData();
    }

    @Test
    public void testCreateUpdateDeleteComment() {

        User user1 = createUser();
        userService.createUser(user1);
        User user2 = createUser();
        userService.createUser(user2);
        User user3 = createUser();
        userService.createUser(user3);
        User user4 = createUser();
        userService.createUser(user4);

        user1 = userService.getUserEagerlyById(user1.getId()).block();
        assert user1 != null;
        user2 = userService.getUserEagerlyById(user2.getId()).block();
        assert user2 != null;
        user3 = userService.getUserEagerlyById(user3.getId()).block();
        assert user3 != null;
        user4 = userService.getUserEagerlyById(user4.getId()).block();
        assert user4 != null;

        Post testedPost = createPost(user1);
        postService.createPost(testedPost);
        testedPost = postService.findPostById(testedPost.getId()).block();
        assert testedPost != null;

        Comment testedComment = createComment(user2, testedPost);
        commentService.createComment(testedComment);
        Mono<Comment> commentMono = commentService.getCommentById(testedComment.getId());
        StepVerifier.create(commentMono)
                .expectNextCount(1)
                .verifyComplete();
        verifyCommentFields(Set.of(testedComment), Set.of(commentMono.block()));
        testedComment = commentMono.block();

        Comment unrepliedComment = createComment(user4, testedPost);
        commentService.createComment(unrepliedComment);
        Mono<Comment> unrepliedCommentMono = commentService.getCommentById(unrepliedComment.getId());
        StepVerifier.create(commentMono)
                .expectNextCount(1)
                .verifyComplete();
        verifyCommentFields(Set.of(unrepliedComment), Set.of(unrepliedCommentMono.block()));
        unrepliedComment = unrepliedCommentMono.block();

        Comment testedReply1 = createCommentReply(user3, testedComment);
        commentService.createComment(testedReply1);
        Mono<Comment> commentReply1Mono = commentService.getCommentById(testedReply1.getId());
        StepVerifier.create(commentReply1Mono)
                .expectNextCount(1)
                .verifyComplete();
        verifyCommentFields(Set.of(testedReply1), Set.of(commentReply1Mono.block()));
        testedReply1 = commentReply1Mono.block();

        Comment testedReply2 = createCommentReply(user2, testedComment);
        commentService.createComment(testedReply2);
        Mono<Comment> commentReply2Mono = commentService.getCommentById(testedReply2.getId());
        StepVerifier.create(commentReply2Mono)
                .expectNextCount(1)
                .verifyComplete();
        verifyCommentFields(Set.of(testedReply2), Set.of(commentReply2Mono.block()));
        testedReply2 = commentReply2Mono.block();

        Comment testedReply3 = createCommentReply(user1, testedReply2);
        commentService.createComment(testedReply3);
        Mono<Comment> commentReply3Mono = commentService.getCommentById(testedReply3.getId());
        StepVerifier.create(commentReply3Mono)
                .expectNextCount(1)
                .verifyComplete();
        verifyCommentFields(Set.of(testedReply3), Set.of(commentReply3Mono.block()));
        testedReply3 = commentReply3Mono.block();

        Flux<Comment> postCommentsMono = commentService.getPostComments(testedPost.getId());
        StepVerifier.create(postCommentsMono)
                .expectNextCount(5)
                .verifyComplete();

        Flux<Comment> user1CommentsMono = commentService.getUserComments(user1.getId());
        StepVerifier.create(user1CommentsMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Comment> user2CommentsMono = commentService.getUserComments(user2.getId());
        StepVerifier.create(user2CommentsMono)
                .expectNextCount(2)
                .verifyComplete();

        Flux<Comment> user3CommentsMono = commentService.getUserComments(user3.getId());
        StepVerifier.create(user3CommentsMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Comment> user4CommentsMono = commentService.getUserComments(user4.getId());
        StepVerifier.create(user4CommentsMono)
                .expectNextCount(1)
                .verifyComplete();

        // test update comment
        String updatedContent = "Updated content";
        testedReply3.setContent(updatedContent);
        Mono<Comment> updatedTestedReply3Mono = commentService.updateComment(testedReply3.getId(), testedReply3);
        verifyCommentFields(Set.of(testedReply3), Set.of(updatedTestedReply3Mono.block()));
        commentReply3Mono = commentService.getCommentById(testedReply3.getId());
        StepVerifier.create(commentReply3Mono)
                .expectNextCount(1)
                .verifyComplete();
        verifyCommentFields(Set.of(testedReply3), Set.of(commentReply3Mono.block()));
        testedReply3 = commentReply3Mono.block();

        // test delete comment
        commentService.deleteComment(unrepliedComment.getId());
        commentService.deleteComment(testedReply3.getId());
        commentService.deleteComment(testedReply1.getId());
        commentService.deleteComment(testedComment.getId());    // this comment just marked as 'deleted' without physical deletion as it has replies.

        postCommentsMono = commentService.getPostComments(testedPost.getId());
        StepVerifier.create(postCommentsMono)
                .expectNextCount(2)
                .verifyComplete();
    }

    private void setupLogger() {
        logger = (Logger) LoggerFactory.getLogger(CommentService.class);
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
