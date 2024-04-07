package com.communiverse.communiverse.services;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.repo.UserRepository;
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

import java.util.Arrays;

import static com.communiverse.communiverse.utils.CreateDataUtils.createUser;
import static com.communiverse.communiverse.utils.CreateDataUtils.createUserWithPostsCommentsLikesFollowers;
import static com.communiverse.communiverse.utils.VerificationResultsUtils.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    private final UserRepository userRepository;
    private final UserService userService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @Autowired
    UserServiceTest(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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

        Mono<User> userMono = userService.getUserById(user.getId());
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
        Mono<User> userMono = userService.getUserById(user.getId());
        verifyCreatedUser(user, userMono);
    }

    @Test
    public void testUpdateUser() {
        // Create a source user
        User user = createUserWithPostsCommentsLikesFollowers();
        userRepository.save(user);

        Mono<User> updatedUserMono = userService.updateUser(user.getId(), user);
        verifyUpdatedUser(user, updatedUserMono);

        // Check if created user is saved in the database
        Mono<User> userMono = userService.getUserById(user.getId());
        verifyUpdatedUser(user, userMono);
    }

//    @Test
//    void alterUserDataTest() {
//        // Create a source user
//        User source = createUserWithPostsCommentsLikesFollowers();
//
//        // Create a target user
//        User target = createUser();
//        target.setUserName(source.getUserName());
//
//        // Apply alterations
//        userService.alterUserData(source, target);
//
//        // Check if the target user was updated correctly
//        verifyUserFields(source, target);
//    }

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
        userRepository.save(user);

        User follower1 = createUser();
        userRepository.save(follower1);

        User follower2 = createUser();
        userRepository.save(follower2);

        User follower3 = createUser();
        userRepository.save(follower3);

        // Add followers to the user
        userService.followUser(user.getId(), follower1).block();
        userService.followUser(user.getId(), follower2).block();
        userService.followUser(user.getId(), follower3).block();

        Flux<User> followers = userService.getUserFollowers(user.getId());
        // Retrieve followers and verify
        StepVerifier.create(followers)
                .expectNextCount(3)
                .verifyComplete();

        // Remove follower from the user
        userService.unfollowUser(user.getId(), follower3).block();
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
        userRepository.deleteAllInBatch();
    }
}


