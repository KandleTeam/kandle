package ch.epfl.sdp.kandle;

import com.google.android.gms.tasks.Task;
import org.junit.Test;
import ch.epfl.sdp.kandle.db.MockDatabase;
import java.util.List;

import static org.junit.Assert.*;

public class MockDatabaseTest {

    private static User antoine = new User("42", "Antoine", "antoine@kandle.ch");
    private static User anas = new User("43", "Anas", "anas@kandle.ch");
    private static User marc = new User("44", "Marc", "marc@kandle.ch");
    private static User louis = new User("409", "Louis", "louis@kandle.ch");
    private static User yanis = new User("451", "Yanis", "yanis@kandle.ch");
    private static User nicolas = new User("480", "Nicolas", "nicolas@kandle.ch");




    @Test
    public void testUserQueries() {
        MockDatabase db = new MockDatabase();

        assertTrue(db.createUser(antoine).isSuccessful());
        assertTrue(db.createUser(anas).isSuccessful());
        assertTrue(db.createUser(marc).isSuccessful());


        Task<List<User>> task1 = db.searchUsers("An", 10);
        assertTrue(task1.isSuccessful());
        List<User> res = task1.getResult();
        assertTrue(res.contains(anas) && res.contains(antoine));

        Task<User> task2 = db.getUserById("44");
        assertTrue(task2.isSuccessful());
        User u1 = task2.getResult();
        assertEquals(marc, u1);

        Task<User> task3 = db.getUserByName("Antoine");
        assertTrue(task3.isSuccessful());
        User u2 = task3.getResult();
        assertEquals(antoine, u2);

    }

}
