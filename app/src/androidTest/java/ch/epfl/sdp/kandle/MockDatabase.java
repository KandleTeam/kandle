package ch.epfl.sdp.kandle;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.epfl.sdp.kandle.MockInstances.Database;

/**
 *  A mocked database. Upon creation, it contains:
 *  - a single user `admin`, with all-zero userID.
 *  - to be extended for posts, etc...
 */
public class MockDatabase extends Database {

    private static Map<String, User> users;

    public MockDatabase() {
        users = new HashMap<>();
        String adminId = "0000000000000000000000000000"; // 28 zeros
        users.put("adminId", new User(adminId, "admin", "admin@kandle.ch"));
    }


    private Optional<User> findUserByName(String username) {
        for(User user : users.values()) {
            if(user.getUsername().equals(username)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Task<User> getUserByName(String username) {

        TaskCompletionSource<User> task = new TaskCompletionSource<>();

        Optional<User> opt = findUserByName(username);

        if(opt.isPresent()) task.setResult(opt.get());
        else task.setException(new IllegalArgumentException("No such user with username: " + username));

        return task.getTask();

    }

    @Override
    public Task<User> getUserById(String userId) {

        TaskCompletionSource<User> task = new TaskCompletionSource<>();

        if(users.containsKey(userId)) {
            task.setResult(users.get(userId));
        } else {
            task.setException(new IllegalArgumentException("No such user with id: " + userId));
        }
        return task.getTask();
    }

    @Override
    public Task<Void> createUser(User user) {

        TaskCompletionSource<Void> task = new TaskCompletionSource<>();

        if(users.containsKey(user.getId())) {
            task.setException(new IllegalArgumentException("User with this id already exists"));
        } else if(findUserByName(user.getUsername()).isPresent()) {
            task.setException(new IllegalArgumentException("User with this username already exists"));
        } else {
            users.put(user.getId(), user);
            task.setResult(null);
        }
        return task.getTask();
    }

    @Override
    public Task<List<User>> searchUsers(String prefix, int maxNumber) {
        List<User> results = new ArrayList<>();

        for(User u : users.values()) {
            if(u.getNormalizedUsername().startsWith(prefix)) {
                results.add(u);
            }
        }

        results.sort(new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return u1.getUsername().compareTo(u2.getUsername());
            }
        });

        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        source.setResult(new ArrayList<User>(results.subList(0, maxNumber)));
        return source.getTask();

    }

    @Override
    public Task<Void> follow(User userFollowing, User userFollowed) {
        return null;
    }

    @Override
    public Task<Void> unfollow(User userUnFollowing, User userUnFollowed) {
        return null;
    }

    @Override
    public Task<Boolean> isFollwing(User userFollowing, User userFollowed) {
        return null;
    }
}