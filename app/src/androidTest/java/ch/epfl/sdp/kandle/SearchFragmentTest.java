package ch.epfl.sdp.kandle;

import android.net.Uri;
import android.os.Parcel;
import android.view.Gravity;
import android.view.View;

import com.google.android.gms.internal.firebase_auth.zzff;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.zzy;
import com.google.firebase.auth.zzz;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;

import androidx.test.rule.ActivityTestRule;
import ch.epfl.sdp.kandle.Fragment.SearchFragment;
import static org.mockito.Mockito.*;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class SearchFragmentTest {


    @Rule
    public final ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);


    @Before
    public void loadFragment(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.follow));
    }

/*
    @Test
    public void clickOnUserItem() throws InterruptedException {

        FirebaseDatabase mockFirebaseDatabase = Mockito.mock(FirebaseDatabase.class);
        FirebaseAuth mockFirebaseAuth = Mockito.mock (FirebaseAuth.class);

        final FirebaseUser mockFirebaseUser = new FirebaseUser() {
            @NonNull
            @Override
            public String getUid() {
                return "firebaseId";
            }

            @NonNull
            @Override
            public String getProviderId() {
                return null;
            }

            @Override
            public boolean isAnonymous() {
                return false;
            }

            @Nullable
            @Override
            public List<String> zza() {
                return null;
            }

            @NonNull
            @Override
            public List<? extends UserInfo> getProviderData() {
                return null;
            }

            @NonNull
            @Override
            public FirebaseUser zza(@NonNull List<? extends UserInfo> list) {
                return null;
            }

            @Override
            public FirebaseUser zzb() {
                return null;
            }

            @NonNull
            @Override
            public FirebaseApp zzc() {
                return null;
            }

            @Nullable
            @Override
            public String getDisplayName() {
                return null;
            }

            @Nullable
            @Override
            public Uri getPhotoUrl() {
                return null;
            }

            @Nullable
            @Override
            public String getEmail() {
                return null;
            }

            @Nullable
            @Override
            public String getPhoneNumber() {
                return null;
            }

            @Nullable
            @Override
            public String zzd() {
                return null;
            }

            @NonNull
            @Override
            public zzff zze() {
                return null;
            }

            @Override
            public void zza(@NonNull zzff zzff) {

            }

            @NonNull
            @Override
            public String zzf() {
                return null;
            }

            @NonNull
            @Override
            public String zzg() {
                return null;
            }

            @Nullable
            @Override
            public FirebaseUserMetadata getMetadata() {
                return null;
            }

            @NonNull
            @Override
            public zzz zzh() {
                return null;
            }

            @Override
            public void zzb(List<zzy> list) {

            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }

            @Override
            public boolean isEmailVerified() {
                return false;
            }
        };

        DatabaseReference mockDatabaseReferenceUsers = Mockito.mock(DatabaseReference.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                DataSnapshot mockUser0 = Mockito.mock(DataSnapshot.class);
                when(mockUser0.getValue(User.class)).thenReturn(new User("firebaseId", "firebaseFullname", "firebaseEmail"));

                DataSnapshot mockUser1 = Mockito.mock(DataSnapshot.class);
                when(mockUser1.getValue(User.class)).thenReturn(new User("1", "test1", "emailtest1"));


                DataSnapshot mockUser2 = Mockito.mock(DataSnapshot.class);
                when(mockUser2.getValue(User.class)).thenReturn(new User("2", "test2", "emailtest2"));

                List<DataSnapshot> users = Arrays.asList(mockUser0, mockUser1, mockUser2);

                ValueEventListener valueEventListener = (ValueEventListener) invocation.getArguments()[0];

                DataSnapshot mockedDataSnapshot = Mockito.mock(DataSnapshot.class);

                when(mockedDataSnapshot.getChildren()).thenReturn( users);


                valueEventListener.onDataChange(mockedDataSnapshot);

                return null;
            }
        }).when(mockDatabaseReferenceUsers).addValueEventListener(any (ValueEventListener.class));




        Mockito.when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        Mockito.when(mockFirebaseDatabase.getReference("Users")).thenReturn(mockDatabaseReferenceUsers);
        //Mockito.when(mockFirebaseDatabase.getReference().child("Follow").child("firebaseId").child("following")).thenReturn(mockDatabaseReferenceFollowFirebaseUser);
        //Mockito.when(mockFirebaseDatabase.getReference().child("Follow").child("test1").child("followers")).thenReturn(mockDatabaseReferenceFollowUser1);
        //Mockito.when(mockFirebaseDatabase.getReference().child("Follow").child("test2").child("followers")).thenReturn(mockDatabaseReferenceFollowUser2);



        FragmentManager fragmentManager = this.mainActivityRule.getActivity().getSupportFragmentManager();

        SearchFragment fragment = SearchFragment.newInstance(mockFirebaseAuth, mockFirebaseDatabase);

        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        //ArrayList<User>users = frag.getUserList();
        Thread.sleep(5000);

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, MyViewAction.clickChildViewWithId(R.id.btn_follow)));

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, MyViewAction.clickChildViewWithId(R.id.btn_follow)));

    }


    public static class MyViewAction {

        public static ViewAction clickChildViewWithId(final int id) {
            return new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "Click on a child view with specified id.";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    View v = view.findViewById(id);
                    v.performClick();
                }
            };
        }

    }
*/


}
