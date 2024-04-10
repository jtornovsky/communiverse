package com.communiverse.communiverse.services;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.repo.CommentRepository;
import com.communiverse.communiverse.repo.LikeRepository;
import com.communiverse.communiverse.repo.PostRepository;
import com.communiverse.communiverse.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import java.util.Arrays;

import static com.communiverse.communiverse.utils.CreateInMemoryDataUtils.*;
import static com.communiverse.communiverse.utils.VerificationResultsUtils.*;

//@Disabled("Disabled until issue #??? is fixed")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @Autowired
    UserServiceTest(UserRepository userRepository, UserService userService,
                    CommentRepository commentRepository, LikeRepository likeRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
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
        // Create a test user
        User user = createUser();
        userRepository.save(user);

        Mono<User> userMono = userService.getUserEagerlyById(user.getId());
        verifyCreatedUser(user, userMono);
    }

    @Test
    public void testGetAllUsers() {
        // Create some test users
        userRepository.saveAll(Arrays.asList(createUser(), createUser(), createUser(), createUser()));

        // Test getAllUsers method
        Flux<User> result = userService.getAllUsers();
        StepVerifier.create(result)
                .expectNextCount(4)
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
    public void testUpdateUser() {
        // Create a source user
        User user = createUser();
        userRepository.save(user);
        Mono<User> userMono = userService.getUserById(user.getId());
        user = userService.getUserEagerlyById(user.getId()).block();

        Post post1 = createPost(user);
        Post post2 = createPost(user);
        Post post3 = createPost(user);
        postRepository.saveAll(Arrays.asList(post1, post2, post3));

        Mono<User> updatedUserMono = userService.updateUser(user.getId(), user);
        StepVerifier.create(updatedUserMono)
                .expectNextCount(1)
                .verifyComplete();

        // Check if created user is saved in the database
        updatedUserMono = userService.getUserEagerlyById(user.getId());
        verifyUpdatedUser(user, updatedUserMono);
    }

    @Test
    void alterUserDataTest() {
        // Create a source user
        User source = createUserWithPostsCommentsLikesFollowers();

        // Create a target user
        User target = createUser();
        target.setUserName(source.getUserName());

        // Apply alterations
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
        User follower1 = createUser();
        User follower2 = createUser();
        User follower3 = createUser();
        userRepository.saveAll(Arrays.asList(user, follower1, follower2, follower3));

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
        // deleteAll does not cascade, so used regular delete
        for (User user : userRepository.findAll()) {
            userRepository.delete(user);
        }
    }
}


