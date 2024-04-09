package com.communiverse.communiverse.model;

import com.communiverse.communiverse.model.like.LikeOnComment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Comparable<Comment> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // For comment replies

    @OneToMany(mappedBy = "parentComment", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Comment> replies = new TreeSet<>(); // For comment replies

    @OneToMany(mappedBy = "comment", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<LikeOnComment> likes = new TreeSet<>();    // For comment likes

    @Column(name = "created", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime created = LocalDateTime.now(ZoneOffset.UTC);

    @Column(name = "modified", nullable = false)
    @LastModifiedDate
    private LocalDateTime modified = LocalDateTime.now(ZoneOffset.UTC);

    @Override
    public int compareTo(@NotNull Comment other) {
        // Compare comments based on their creation date
        return this.created.compareTo(other.created);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment comment)) return false;

        return Objects.equals(id, comment.id)
                && Objects.equals(content, comment.content)
                && Objects.equals(user, comment.user)
                && Objects.equals(post, comment.post)
                && Objects.equals(parentComment, comment.parentComment);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(content);
        result = 31 * result + Objects.hashCode(user);
        result = 31 * result + Objects.hashCode(post);
        result = 31 * result + Objects.hashCode(parentComment);
        return result;
    }
}
