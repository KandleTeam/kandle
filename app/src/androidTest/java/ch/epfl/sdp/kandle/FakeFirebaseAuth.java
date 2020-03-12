package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.internal.firebase_auth.zzff;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.internal.IdTokenListener;
import com.google.firebase.auth.internal.InternalAuthProvider;
import com.google.firebase.auth.zzy;
import com.google.firebase.auth.zzz;

import java.util.List;
import java.util.concurrent.Executor;

public class FakeFirebaseAuth extends FirebaseAuth {


    private boolean isConnected = false;


    public FakeFirebaseAuth(FirebaseApp firebaseApp) {
        super(firebaseApp);
    }



    @NonNull
    @Override
    public Task<GetTokenResult> getAccessToken(boolean b) {
        return null;
    }

    @Nullable
    @Override
    public String getUid() {
        return "MockId";
    }

    @Override
    public void addIdTokenListener(@NonNull IdTokenListener idTokenListener) {

    }

    @Override
    public void removeIdTokenListener(@NonNull IdTokenListener idTokenListener) {

    }

    public Task<AuthResult> signInWithEmailAndPassword(@NonNull String s,@NonNull String s1) {

        isConnected = true;

        System.out.println("done");
        return new Task<AuthResult>() {
            @Override
            public boolean isComplete() {

                System.out.println("getc");
                return true;
            }

            @Override
            public boolean isSuccessful()
            {
                System.out.println("gets");
                return true;
            }

            @Override
            public boolean isCanceled() {

                System.out.println("getcled");
                return false;
            }

            @Nullable
            @Override
            public AuthResult getResult() {
                System.out.println("getresult");

                return null;
            }

            @Nullable
            @Override
            public <X extends Throwable> AuthResult getResult(@NonNull Class<X> aClass) throws X {
                return null;
            }

            @Nullable
            @Override
            public Exception getException() {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnSuccessListener(@NonNull OnSuccessListener<? super AuthResult> onSuccessListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super AuthResult> onSuccessListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super AuthResult> onSuccessListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
                return null;
            }
        };
    }

    public FirebaseUser getCurrentUser() {

        if (isConnected) {


            return new FirebaseUser() {
                @NonNull
                @Override
                public String getUid() {
                    return "MockID";
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

        }
        else return null;
    }

    public Task<AuthResult> createUserWithEmailAndPassword(@NonNull String s,@NonNull String s1){
        return new Task<AuthResult>() {
            @Override
            public boolean isComplete() {
                return false;
            }

            @Override
            public boolean isSuccessful() {
                return true;
            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Nullable
            @Override
            public AuthResult getResult() {
                return null;
            }

            @Nullable
            @Override
            public <X extends Throwable> AuthResult getResult(@NonNull Class<X> aClass) throws X {
                return null;
            }

            @Nullable
            @Override
            public Exception getException() {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnSuccessListener(@NonNull OnSuccessListener<? super AuthResult> onSuccessListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super AuthResult> onSuccessListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super AuthResult> onSuccessListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<AuthResult> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
                return null;
            }
        };

    }


}
