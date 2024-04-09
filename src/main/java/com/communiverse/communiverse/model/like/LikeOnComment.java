package com.communiverse.communiverse.model.like;

import com.communiverse.communiverse.model.Comment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikeOnComment that)) return false;
        if (!super.equals(o)) return false;

        return Objects.equals(comment, that.comment) && super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(comment);
        return result;
    }
}
