package com.communiverse.communiverse.model.like;

import com.communiverse.communiverse.model.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikeOnPost that)) return false;
        if (!super.equals(o)) return false;

        return Objects.equals(post, that.post) && super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(post);
        return result;
    }
}
