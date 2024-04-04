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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        verifyUser(user, userMono);
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
        verifyUser(user, userMono);
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



    private User createUser() {

        String textData = UUID.randomUUID().toString();
        String userName = textData.substring(0, 7);
        String password = textData.substring(8);

        User user = new User();
        user.setUserName(userName);
        user.setEmail(userName + "@email.com");
        user.setPassword(password);
        user.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        user.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        user.setLastLogin(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return user;
    }

    private void verifyUser(User expectedUser, Mono<User> actualUserMono) {
        /*
         using reactive testing techniques with StepVerifier to handle
         asynchronous operations in a more concise and efficient way
         */
        StepVerifier.create(actualUserMono)
                .assertNext(actualUser -> {
                    assertNotNull(actualUser);
                    assertEquals(expectedUser.getUserName(), actualUser.getUserName());
                    assertEquals(expectedUser.getEmail(), actualUser.getEmail());
                    assertEquals(expectedUser.getPassword(), actualUser.getPassword());
                    assertEquals(expectedUser.getModified(), actualUser.getModified());
                    assertEquals(expectedUser.getCreated(), actualUser.getCreated());
                    assertEquals(expectedUser.getLastLogin(), actualUser.getLastLogin());
                })
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


