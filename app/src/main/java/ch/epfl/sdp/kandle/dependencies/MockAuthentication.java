package ch.epfl.sdp.kandle.dependencies;

import android.net.Uri;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.internal.firebase_auth.zzff;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.zzy;
import com.google.firebase.auth.zzz;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.kandle.User;

public class MockAuthentication implements Authentication {


    private List<String> emails = new ArrayList<>();
    private MockDatabase database;
    private boolean isConnected;

    public MockAuthentication(boolean isConnected) {
        this.isConnected = isConnected;
        emails.add("user1@kandle.ch");
        database = new MockDatabase();
    }

    @Override
    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<AuthResult>();

        if (emails.contains(email)){
            isConnected = false;
            source.setException( new Exception("You already have an account") );
        }
        else {
            emails.add(email);
            isConnected = true;
            String newId = "newUserId";
            source.setResult(getAuthResultWithUser(newId,email));
            database.users.put(newId,new User(newId,"newUser","newUser@kandle.ch","newFullName",null));

        }
        return source.getTask();
    }

    @Override
    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<AuthResult>();
        if (emails.contains(email)) {
            isConnected = true;
            User user = findUserinDatabasebyEmail(email);
            source.setResult(getAuthResultWithUser(findUserinDatabasebyEmail(email).getId(),email));

        }

        else {
            isConnected = false;
            source.setException(new Exception ("You do not have an account yet"));
        }

        return source.getTask();

    }

    @Override
    public void signOut() {
        isConnected=false;
    }

    public AuthenticationUser getCurrentUser() {
        if (isConnected){
            return new MockAuthenticationUser();
        }
        else {
            return null;
        }
    }


    private AuthResult getAuthResultWithUser(String id, String email){
        AuthResult authResult = new AuthResult() {
            @Nullable
            @Override
            public FirebaseUser getUser() {
                return new FirebaseUser() {
                    @NonNull
                    @Override
                    public String getUid() {
                        return id;
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
                        return email;
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
            }

            @Nullable
            @Override
            public AdditionalUserInfo getAdditionalUserInfo() {
                return null;
            }

            @Nullable
            @Override
            public AuthCredential getCredential() {
                return null;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }
        };
        return authResult;
    }

    private User findUserinDatabasebyEmail(String email) {
        for(User user : database.users.values()){
            if(user.getEmail().equals(email)){
                return user;
            }
        }
        return null;
    }
}
