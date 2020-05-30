package ch.epfl.sdp.kandle.storage.room;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.user.User;

public class UserWithPosts {
    @Embedded
    public User user;
    @Relation(
            parentColumn = "id",
            entityColumn = "userId",
            entity = Post.class
    )
    public List<Post> posts;
}
