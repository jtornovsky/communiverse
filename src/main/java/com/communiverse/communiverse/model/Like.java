package com.communiverse.communiverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "like")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "created", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime created = LocalDateTime.now(ZoneOffset.UTC);

    @Column(name = "modified", nullable = false)
    @LastModifiedDate
    private LocalDateTime modified = LocalDateTime.now(ZoneOffset.UTC);
}

