package com.communiverse.communiverse.model.like;

import com.communiverse.communiverse.model.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "like_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeOnPost extends Like {

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
