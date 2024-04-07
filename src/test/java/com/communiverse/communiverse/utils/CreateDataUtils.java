package com.communiverse.communiverse.utils;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Like;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CreateDataUtils {

    public static User createUserWithPostsCommentsLikesFollowers() {

        User user = createUser();
        Post post1 = createPost(user);
        Post post2 = createPost(user);
        Post post3 = createPost(user);

        createComment(createUser(), post1);
        createComment(createUser(), post1);
        createComment(createUser(), post1);
        createComment(createUser(), post1);

        createComment(createUser(), post2);
        createComment(createUser(), post2);
        createComment(createUser(), post2);

        User replyingUser = createUser();
        Comment comment = createComment(replyingUser, post3);
        Comment commentWithReplies1 = createCommentReply(user, comment);
        Comment commentWithReplies2 = createCommentReply(replyingUser, commentWithReplies1);
        Comment commentWithReplies3 = createCommentReply(user, commentWithReplies2);

        createPostLike(createUser(), post1);
        createPostLike(replyingUser, post1);
        createPostLike(createUser(), post3);

        createCommentLike(replyingUser, commentWithReplies1);
        createCommentLike(user, comment);

        User follower1 = createUser();
        User follower2 = createUser();
        follower1.setFollowers(Set.of(follower2, user));
        follower2.setFollowers(Set.of(replyingUser));
        replyingUser.setFollowers(Set.of(user, follower1));
        Set<User> followers = new HashSet<>(Arrays.asList(follower1, follower2, replyingUser));
        user.setFollowers(followers);

        return user;
    }

    public static User createUser() {

        String textData = UUID.randomUUID().toString();
        String userName = textData.substring(0, 10);
        String password = textData.substring(8);

        User user = new User();
        user.setUserName(userName);
        user.setEmail(userName + "@email.com");
        user.setPassword(password);
        user.setProfilePicture(textData);
        user.setLastLogin(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        user.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        user.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return user;
    }

    public static Post createPost(User user) {

        String textData = "post_" + UUID.randomUUID() + "_" + UUID.randomUUID();

        Post post = new Post();
        post.setTitle(textData.substring(0, 7));
        post.setContent(textData);
        post.setImage(textData.substring(10));
        post.setUser(user);

        post.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        post.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        user.getPosts().add(post);

        return post;
    }

    public static Comment createComment(User user, Post post) {

        String textData = "comment_on_post_" + post.getId() + "_" + UUID.randomUUID() + "_" + UUID.randomUUID();

        Comment comment = new Comment();
        comment.setContent(textData);
        comment.setPost(post);
        comment.setUser(user);

        comment.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        comment.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        post.getComments().add(comment);

        return comment;
    }

    public static Comment createCommentReply(User user, Comment parentComment) {
        String textData = "reply_on_comment_" + parentComment.getId() + "_" + UUID.randomUUID() + "_" + UUID.randomUUID();

        Comment reply = new Comment();
        reply.setContent(textData);
        reply.setUser(user);
        reply.setPost(parentComment.getPost());
        reply.setParentComment(parentComment);

        reply.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        reply.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        parentComment.getReplies().add(reply);

        return reply;
    }

    public static Like createPostLike(User user, Post post) {

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);

        like.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        like.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        post.getLikes().add(like);

        return like;
    }

    public static Like createCommentLike(User user, Comment comment) {

        Like like = new Like();
        like.setUser(user);
        like.setComment(comment);

        like.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        like.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        comment.getLikes().add(like);

        return like;
    }
}
