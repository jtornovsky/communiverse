package com.communiverse.communiverse.utils;

import com.communiverse.communiverse.model.User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

public class VerificationResultsUtils {

    public static void verifyCreatedUser(User expectedUser, Mono<User> actualUserMono) {
        /*
         using reactive testing techniques with StepVerifier to handle
         asynchronous operations in a more concise and efficient way
         */
        StepVerifier.create(actualUserMono)
                .assertNext(actualUser -> {
                    verifyUserFields(expectedUser, actualUser);
                    assertEquals(expectedUser.getModified(), actualUser.getModified());
                })
                .verifyComplete();
    }

    public static void verifyUpdatedUser(User expectedUser, Mono<User> actualUserMono) {
        StepVerifier.create(actualUserMono)
                .assertNext(actualUser -> {
                    verifyUserFields(expectedUser, actualUser);
                    assertTrue(expectedUser.getModified().isBefore(actualUser.getModified()));
                })
                .verifyComplete();
    }

    public static void verifyUserFields(User expectedUser, User actualUser) {
        assertNotNull(actualUser);
        assertEquals(expectedUser.getUserName(), actualUser.getUserName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getPassword(), actualUser.getPassword());
        assertEquals(expectedUser.getProfilePicture(), actualUser.getProfilePicture());
        assertEquals(expectedUser.getCreated(), actualUser.getCreated());
        assertEquals(expectedUser.getLastLogin(), actualUser.getLastLogin());
        assertEquals(expectedUser.getComments(), actualUser.getComments());
        assertEquals(expectedUser.getPosts(), actualUser.getPosts());
        assertEquals(expectedUser.getLikesOnPosts(), actualUser.getLikesOnPosts());
        assertEquals(expectedUser.getLikesOnComments(), actualUser.getLikesOnComments());
        assertEquals(expectedUser.getFollowers(), actualUser.getFollowers());
    }
}
