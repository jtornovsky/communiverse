package com.communiverse.communiverse.utils;

import com.communiverse.communiverse.model.Comment;
import com.communiverse.communiverse.model.Like;
import com.communiverse.communiverse.model.Post;
import com.communiverse.communiverse.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CreateDataUtils {

    public static User createUser() {

        String textData = UUID.randomUUID().toString();
        String userName = textData.substring(0, 7);
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

        Set<Comment> comments = Set.of(createComment(post, user), createComment(post, user), createComment(post, user), createComment(post, user));
        post.setComments(comments);

        post.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        post.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return post;
    }

    public static Comment createComment(Post post, User user) {

        String textData = "comment_" + UUID.randomUUID() + "_" + UUID.randomUUID();

        Comment comment = new Comment();
        comment.setContent(textData);
        comment.setPost(post);
        comment.setUser(user);

        comment.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        comment.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

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

        return reply;
    }

    public static Like createPostLike(User user, Post post) {

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);

        like.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        like.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return like;
    }

    public static Like createCommentLike(User user, Comment comment) {

        Like like = new Like();
        like.setUser(user);
        like.setComment(comment);
        like.setPost(comment.getPost());

        like.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        like.setModified(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return like;
    }
}
