package ch.epfl.sdp.kandle.Storage.room;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.User;

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
