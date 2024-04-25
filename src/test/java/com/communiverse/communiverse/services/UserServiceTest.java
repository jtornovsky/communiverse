package com.communiverse.communiverse.services;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.model.like.Like;
import com.communiverse.communiverse.model.like.LikeOnComment;
import com.communiverse.communiverse.model.like.LikeOnPost;
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

import java.util.stream.IntStream;

import static com.communiverse.communiverse.utils.CreateDataUtils.*;
import static com.communiverse.communiverse.utils.VerificationResultsUtils.*;

//@Disabled("Disabled until issue #??? is fixed")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

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
    UserServiceTest(UserRepository userRepository, UserService userService, CommentRepository commentRepository, CommentService commentService,
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
    public void testGetUserById() {
        User user = createUser();
        userService.createUser(user);

        Mono<User> userMono = userService.getUserEagerlyById(user.getId());
        verifyCreatedUser(user, userMono);
    }

    @Test
    public void testGetAllUsers() {

        final int NUMBER_OF_USERS = 4;

        IntStream.range(0, NUMBER_OF_USERS)
                .mapToObj(i -> createUser())
                .forEach(userService::createUser);

        // Test getAllUsers method
        Flux<User> result = userService.getAllUsers();
        StepVerifier.create(result)
                .expectNextCount(NUMBER_OF_USERS)
                .verifyComplete();
    }

    @Test
    public void testCreateUser() {
        // Create a test user
        User user = createUser();

        // Test createUser method
        Mono<User> result = userService.createUser(user);
        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();

        // Check if created user is saved in the database
        Mono<User> userMono = userService.getUserEagerlyById(user.getId());
        verifyCreatedUser(user, userMono);
    }

    @Test
    public void testUpdateAllUserData() {

        User testedUser = createUser();
        userService.createUser(testedUser);
        User anyUser1 = createUser();
        userService.createUser(anyUser1);
        User anyUser2 = createUser();
        userService.createUser(anyUser2);
        User anyUser3 = createUser();
        userService.createUser(anyUser3);
        User anyUser4 = createUser();
        userService.createUser(anyUser4);
        User anyUser5 = createUser();
        userService.createUser(anyUser5);
        User anyUser6 = createUser();
        userService.createUser(anyUser6);
        User anyUser7 = createUser();
        userService.createUser(anyUser7);
        User replyingUser = createUser();
        userService.createUser(replyingUser);

        testedUser = userService.getUserEagerlyById(testedUser.getId()).block();

        Post post1 = createPost(testedUser);
        postService.createPost(post1);
        Post post2 = createPost(testedUser);
        postService.createPost(post2);
        Post post3 = createPost(testedUser);
        postService.createPost(post3);
        Post post4 = createPost(anyUser1);
        postService.createPost(post4);

        Comment comment1 = createComment(anyUser1, post1);
        commentService.createComment(comment1);
        Comment comment2 = createComment(anyUser2, post1);
        commentService.createComment(comment2);
        Comment comment3 = createComment(anyUser3, post1);
        commentService.createComment(comment3);
        Comment comment4 = createComment(anyUser4, post1);
        commentService.createComment(comment4);

        Comment comment5 = createComment(anyUser5, post2);
        commentService.createComment(comment5);
        Comment comment6 = createComment(anyUser6, post2);
        commentService.createComment(comment6);
        Comment comment7 = createComment(anyUser7, post2);
        commentService.createComment(comment7);

        Comment initialComment = createComment(replyingUser, post3);
        commentService.createComment(initialComment);
        Comment commentWithReplies1 = createCommentReply(testedUser, initialComment);
        commentService.createComment(commentWithReplies1);
        Comment commentWithReplies2 = createCommentReply(replyingUser, commentWithReplies1);
        commentService.createComment(commentWithReplies2);
        Comment commentWithReplies3 = createCommentReply(testedUser, commentWithReplies2);
        commentService.createComment(commentWithReplies3);

        LikeOnPost likeOnPost1 = createPostLike(anyUser3, post1);
        likeService.createPostLike(likeOnPost1);
        LikeOnPost likeOnPost2 = createPostLike(replyingUser, post1);
        likeService.createPostLike(likeOnPost2);
        LikeOnPost likeOnPost3 = createPostLike(anyUser4, post3);
        likeService.createPostLike(likeOnPost3);
        LikeOnPost likeOnPost4 = createPostLike(testedUser, post4);
        likeService.createPostLike(likeOnPost4);

        Comment likeOnComment1 = createCommentReply(anyUser7, initialComment);
        commentService.createComment(likeOnComment1);
        LikeOnComment likeOnComment2 = createCommentLike(anyUser6, commentWithReplies1);
        likeService.createCommentLike(likeOnComment2);
        LikeOnComment likeOnComment3 = createCommentLike(anyUser7, initialComment);
        likeService.createCommentLike(likeOnComment3);
        LikeOnComment likeOnComment4 = createCommentLike(testedUser, likeOnComment1);
        likeService.createCommentLike(likeOnComment4);

// TODO: need to understand why the error of Out of memory.; SQL statement happens in case when applying likes on its own comment or post

//        LikeOnComment like7 = createCommentLike(replyingUser, commentWithReplies1);
//        likeService.createCommentLike(like7);
//        LikeOnComment like8 = createCommentLike(testedUser, initialComment);
//        likeService.createCommentLike(like8);

        Mono<User> updatedUserMono = userService.updateUser(testedUser.getId(), testedUser);
        StepVerifier.create(updatedUserMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Post> userPostsMono = postService.getPostsByUserId(testedUser.getId());
        StepVerifier.create(userPostsMono)
                .expectNextCount(3)
                .verifyComplete();

        Flux<Comment> userCommentsMono = commentService.getUserComments(testedUser.getId());
        StepVerifier.create(userCommentsMono)
                .expectNextCount(2)
                .verifyComplete();

        Flux<Like> userLikesOnPostsMono = likeService.getUserPostLikes(testedUser.getId());
        StepVerifier.create(userLikesOnPostsMono)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Like> userLikesOnCommentsMono = likeService.getUserCommentLikes(testedUser.getId());
        StepVerifier.create(userLikesOnCommentsMono)
                .expectNextCount(1)
                .verifyComplete();

        // Check if created user is saved in the database
        updatedUserMono = userService.getUserEagerlyById(testedUser.getId());
        verifyUpdatedUser(testedUser, updatedUserMono);
    }

    @Test
    void testUpdateAllUserDataInMemory() {
        // Create a source user
        User source = createUserWithPostsCommentsLikesFollowers();

        // Create a target user
        User target = createUser();
        target.setUserName(source.getUserName());

        userService.alterUserData(source, target);

        // Check if the target user was updated correctly
        verifyUserFields(source, target);
    }

    @Test
    void testDeleteUser() {
        // Create a test user
        User user = createUser();

        // Test createUser method
        Mono<User> createUserResult = userService.createUser(user);
        StepVerifier.create(createUserResult)
                .expectNext(user)
                .verifyComplete();

        // Call deleteUser method
        Mono<Void> deleteUserResult = userService.deleteUser(user.getId());

        // Use StepVerifier to test the result
        StepVerifier.create(deleteUserResult)
                .verifyComplete();

        // Verify that the user is deleted
        StepVerifier.create(userService.getUserById(user.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testAddAndRemoveFollowers() {

        User user = createUser();
        userService.createUser(user);
        User follower1 = createUser();
        userService.createUser(follower1);
        User follower2 = createUser();
        userService.createUser(follower2);
        User follower3 = createUser();
        userService.createUser(follower3);

        // Add followers to the user
        userService.followUser(user.getId(), follower1.getId());
        userService.followUser(user.getId(), follower2.getId());
        userService.followUser(user.getId(), follower3.getId());

        Flux<User> followers = userService.getUserFollowers(user.getId());
        // Retrieve followers and verify
        StepVerifier.create(followers)
                .expectNextCount(3)
                .verifyComplete();

        // Remove follower from the user
        userService.unfollowUser(user.getId(), follower3.getId());
        // Retrieve followers and verify
        followers = userService.getUserFollowers(user.getId());
        StepVerifier.create(followers)
                .expectNextCount(2)
                .verifyComplete();
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


