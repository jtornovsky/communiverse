package com.communiverse.communiverse.model.like;

import com.communiverse.communiverse.model.Comment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "like_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeOnComment extends Like {

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;
}
