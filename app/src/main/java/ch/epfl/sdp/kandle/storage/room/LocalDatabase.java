package ch.epfl.sdp.kandle.storage.room;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.User;

@Database(entities = {User.class, Post.class}, exportSchema = false, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    private static final String DB_NAME = "kandle_local_db";
    public static final int MAX_USER_IN_DB = 50;
    public static final int MAX_POST_IN_DB = 50;

    private static LocalDatabase instance;

    public static synchronized LocalDatabase getInstance() {
        if (instance == null) {

            instance = Room.databaseBuilder(Kandle.getContext(), LocalDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration().allowMainThreadQueries()
                    .build();
        }

        return instance;
    }

    public abstract UserDao userDao();

    public abstract PostDao postDao();

    public abstract UserWithPostsDao userWithPostsDao();


}
