package com.communiverse.communiverse.utils;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;
import com.communiverse.communiverse.model.like.LikeOnComment;
import com.communiverse.communiverse.model.like.LikeOnPost;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

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
        verifyPostFields(expectedUser.getPosts(), actualUser.getPosts());
        verifyCommentFields(expectedUser.getComments(), actualUser.getComments());
        verifyLikeOnPostFields(expectedUser.getLikeOnPosts(), actualUser.getLikeOnPosts());
        verifyLikeOnCommentFields(expectedUser.getLikeOnComments(), actualUser.getLikeOnComments());
        assertEquals(expectedUser.getFollowers(), actualUser.getFollowers());
    }

    public static void verifyPostFields(@NotNull Set<Post> expectedPosts, @NotNull Set<Post> actualPosts) {
        assertEquals(expectedPosts.size(), actualPosts.size(), "Number of posts mismatch");

        for (Post expectedPost : expectedPosts) {
            assertTrue(actualPosts.contains(expectedPost), "Post not found: " + expectedPost.getTitle());
            Post actualPost = actualPosts.stream()
                    .filter(p -> p.equals(expectedPost))
                    .findFirst()
                    .orElse(null);

            if (actualPost != null) {
                assertEquals(expectedPost.getTitle(), actualPost.getTitle(), "Title mismatch for post: " + expectedPost.getTitle());
                assertEquals(expectedPost.getContent(), actualPost.getContent(), "Content mismatch for post: " + expectedPost.getTitle());
                assertEquals(expectedPost.getImage(), actualPost.getImage(), "Image URL mismatch for post: " + expectedPost.getTitle());
                assertEquals(expectedPost.getUser(), actualPost.getUser(), "User mismatch for post: " + expectedPost.getTitle());
                assertEquals(expectedPost.getComments(), actualPost.getComments(), "Comments mismatch for post: " + expectedPost.getTitle());
                assertEquals(expectedPost.getLikes(), actualPost.getLikes(), "Likes mismatch for post: " + expectedPost.getTitle());
                assertEquals(expectedPost.getCreated(), actualPost.getCreated(), "Created date mismatch for post: " + expectedPost.getTitle());
                assertEquals(expectedPost.getModified(), actualPost.getModified(), "Modified date mismatch for post: " + expectedPost.getTitle());
            }
        }
    }

    public static void verifyCommentFields(@NotNull Set<Comment> expectedComments, @NotNull Set<Comment> actualComments) {
        assertEquals(expectedComments.size(), actualComments.size(), "Number of comments mismatch");

        for (Comment expectedComment : expectedComments) {
            assertTrue(actualComments.contains(expectedComment), "Comment not found: " + expectedComment.getId());
            Comment actualComment = actualComments.stream()
                    .filter(c -> c.getContent().equals(expectedComment.getContent()))
                    .findFirst()
                    .orElse(null);

            if (actualComment != null) {
                assertEquals(expectedComment.getContent(), actualComment.getContent(), "Content mismatch for comment: " + expectedComment.getContent());
                assertEquals(expectedComment.getUser(), actualComment.getUser(), "User mismatch for comment: " + expectedComment.getContent());
                assertEquals(expectedComment.getPost(), actualComment.getPost(), "Post mismatch for comment: " + expectedComment.getContent());
                assertEquals(expectedComment.getParentComment(), actualComment.getParentComment(), "Parent comment mismatch for comment: " + expectedComment.getContent());
                assertEquals(expectedComment.getReplies(), actualComment.getReplies(), "Replies mismatch for comment: " + expectedComment.getContent());
                assertEquals(expectedComment.getLikes(), actualComment.getLikes(), "Likes mismatch for comment: " + expectedComment.getContent());
                assertEquals(expectedComment.getCreated(), actualComment.getCreated(), "Created date mismatch for comment: " + expectedComment.getContent());
                assertEquals(expectedComment.getModified(), actualComment.getModified(), "Modified date mismatch for comment: " + expectedComment.getContent());
            }
        }
    }

    public static void verifyLikeOnPostFields(@NotNull Set<LikeOnPost> expectedLikeOnPosts, @NotNull Set<LikeOnPost> actualLikeOnPosts) {
        assertEquals(expectedLikeOnPosts.size(), actualLikeOnPosts.size(), "Number of like on posts mismatch");

        for (LikeOnPost expectedLikeOnPost : expectedLikeOnPosts) {
            assertTrue(actualLikeOnPosts.contains(expectedLikeOnPost), "Like on post not found: " + expectedLikeOnPost.getId());
            LikeOnPost actualLikeOnPost = actualLikeOnPosts.stream()
                    .filter(l -> l.getPost().getTitle().equals(expectedLikeOnPost.getPost().getTitle()))
                    .findFirst()
                    .orElse(null);

            if (actualLikeOnPost != null) {
                assertEquals(expectedLikeOnPost.getUser(), actualLikeOnPost.getUser(), "User mismatch for like on post: " + expectedLikeOnPost.getPost().getTitle());
                assertEquals(expectedLikeOnPost.getPost(), actualLikeOnPost.getPost(), "Post mismatch for like on post: " + expectedLikeOnPost.getPost().getTitle());
                assertEquals(expectedLikeOnPost.getCreated(), actualLikeOnPost.getCreated(), "Created date mismatch for like on post: " + expectedLikeOnPost.getPost().getTitle());
                assertEquals(expectedLikeOnPost.getModified(), actualLikeOnPost.getModified(), "Modified date mismatch for like on post: " + expectedLikeOnPost.getPost().getTitle());
            }
        }
    }

    public static void verifyLikeOnCommentFields(@NotNull Set<LikeOnComment> expectedLikeOnComments, @NotNull Set<LikeOnComment> actualLikeOnComments) {
        assertEquals(expectedLikeOnComments.size(), actualLikeOnComments.size(), "Number of like on comments mismatch");

        for (LikeOnComment expectedLikeOnComment : expectedLikeOnComments) {
            assertTrue(actualLikeOnComments.contains(expectedLikeOnComment), "Like on comment not found: " + expectedLikeOnComment.getId());
            LikeOnComment actualLikeOnComment = actualLikeOnComments.stream()
                    .filter(l -> l.getComment().getContent().equals(expectedLikeOnComment.getComment().getContent()))
                    .findFirst()
                    .orElse(null);

            if (actualLikeOnComment != null) {
                assertEquals(expectedLikeOnComment.getUser(), actualLikeOnComment.getUser(), "User mismatch for like on comment: " + expectedLikeOnComment.getComment().getContent());
                assertEquals(expectedLikeOnComment.getComment(), actualLikeOnComment.getComment(), "Comment mismatch for like on comment: " + expectedLikeOnComment.getComment().getContent());
                assertEquals(expectedLikeOnComment.getCreated(), actualLikeOnComment.getCreated(), "Created date mismatch for like on comment: " + expectedLikeOnComment.getComment().getContent());
                assertEquals(expectedLikeOnComment.getModified(), actualLikeOnComment.getModified(), "Modified date mismatch for like on comment: " + expectedLikeOnComment.getComment().getContent());
            }
        }
    }
}

