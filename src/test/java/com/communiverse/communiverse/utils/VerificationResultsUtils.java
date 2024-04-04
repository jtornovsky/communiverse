package com.communiverse.communiverse.utils;

import com.communiverse.communiverse.model.User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

public class VerificationResultsUtils {

    public static void verifyUser(User expectedUser, Mono<User> actualUserMono) {
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
                    assertEquals(expectedUser.getProfilePicture(), actualUser.getProfilePicture());
                    assertEquals(expectedUser.getModified(), actualUser.getModified());
                    assertEquals(expectedUser.getCreated(), actualUser.getCreated());
                    assertEquals(expectedUser.getLastLogin(), actualUser.getLastLogin());
                })
                .verifyComplete();
    }

    public static void verifyUpdatedUser(User expectedUser, Mono<User> actualUserMono) {
        StepVerifier.create(actualUserMono)
                .assertNext(actualUser -> {
                    assertNotNull(actualUser);
                    assertEquals(expectedUser.getUserName(), actualUser.getUserName());
                    assertEquals(expectedUser.getEmail(), actualUser.getEmail());
                    assertEquals(expectedUser.getPassword(), actualUser.getPassword());
                    assertEquals(expectedUser.getProfilePicture(), actualUser.getProfilePicture());
                    assertTrue(expectedUser.getModified().isBefore(actualUser.getModified()));
                    assertEquals(expectedUser.getCreated(), actualUser.getCreated());
                    assertEquals(expectedUser.getLastLogin(), actualUser.getLastLogin());
                })
                .verifyComplete();
    }
}
