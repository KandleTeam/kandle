package ch.epfl.sdp.kandle;

import androidx.room.Room;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ch.epfl.sdp.kandle.storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;

public class RoomTest {

    private LocalDatabase localDatabase;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class,true,true
            ){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network,localDatabase);
                }
            };

    @Rule
    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);


    @After
    public void clearCurrentUserAndLocalDb(){
        LoggedInUser.clear();
        localDatabase.close();
    }


    @Test
    public void canGetBasicRoomFeatures(){
        localDatabase.getOpenHelper().getDatabaseName();
        localDatabase.getOpenHelper().getReadableDatabase();
        localDatabase.getOpenHelper().getWritableDatabase();
        localDatabase.getQueryExecutor();
        localDatabase.getTransactionExecutor();
        localDatabase.getOpenHelper().setWriteAheadLoggingEnabled(true);

    }

    @Test
    public void canExecuteBasicOperationsOnUserTable(){
        for (int i = 3; i < 54; i++) {
            localDatabase.userDao().insertUser(new User("user"+i+"Id", "user" + i, "user" + i+"@kandle.ch", "user"+i, null));
        }
        List<User> allPostsLocally = localDatabase.userDao().getUserList();
        Assert.assertEquals(allPostsLocally.size(), 51);
        localDatabase.userDao().storeOnly50Users();
        allPostsLocally = localDatabase.userDao().getUserList();
        Assert.assertEquals(50, allPostsLocally.size());
        localDatabase.clearAllTables();
        Assert.assertEquals(Collections.emptyList(), localDatabase.userDao().getUserList());
        localDatabase.userDao().getUserFromUserId("id");
        localDatabase.userWithPostsDao().getAllUserWithPosts();

    }

    @Test
    public void checkPostUserRealtion(){
        localDatabase.userDao().insertUser(new User("user1Id", "user1", "user1@kandle.ch", "user1", null));
        for(int i = 0; i < 40; i++) {
            localDatabase.postDao().insertPost(new Post("hey", null, new Date(), "user1Id", Integer.toString(i)));
        }
        localDatabase.userWithPostsDao().getAllUserWithPosts();

    }

    @Test
    public void canOnlyHave50PostLocallyAndRemovesTheRest() {
        for (int i = 3; i < 54; i++) {
            localDatabase.postDao().insertPost(new Post("Hello", null, new Date(), LoggedInUser.getInstance().getId(), "post" + i + "Id"));
        }
        List<Post> allPostsLocally = localDatabase.postDao().getPostList();
        Assert.assertEquals(52,allPostsLocally.size() );
        localDatabase.postDao().storeOnly50Posts();
        allPostsLocally = localDatabase.postDao().getPostList();
        Assert.assertEquals(50, allPostsLocally.size());
        localDatabase.clearAllTables();
        Assert.assertEquals(Collections.emptyList(), localDatabase.postDao().getPostList());
        localDatabase.postDao().getPostFromPostId("id");
    }


}
