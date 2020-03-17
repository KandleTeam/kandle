package ch.epfl.sdp.kandle;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *  A mocked database. Upon creation, it contains:
 *  - a single user `admin`, with all-zero userID.
 *  - to be extended for posts, etc...
 */
public class MockDatabase implements Database {

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
}
