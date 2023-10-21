package com.elderwatch.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityGoogleLoginBinding;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GoogleLoginActivity extends AppCompatActivity {

    ActivityGoogleLoginBinding binding;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    ActivityResultLauncher<IntentSenderRequest> oneTapLauncher;
    ProgressDialog pdLoading;
    FSRequest request;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoogleLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pdLoading = new ProgressDialog(GoogleLoginActivity.this);
        pdLoading.setMessage("Loading ...");
        pdLoading.setCancelable(false);
        request = new FSRequest();
        startSignIn();
    }

    private void startSignIn() {
        oneTapLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                SignInCredential credential = null;
                try {
                    credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String idToken = credential.getGoogleIdToken();
                    String username = credential.getId();
                    if (idToken != null) {
                        pdLoading.show();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String currentDate = sdf.format(new Date());
                        Users users = new Users.UserBuilder()
                                .setEmail(username)
                                .setFirstName(credential.getGivenName())
                                .setLastName(credential.getFamilyName())
                                .setPassword("default")
                                .setPhoneNumber(credential.getPhoneNumber())
                                .setRegisteredDate(currentDate)
                                .setUserType(2)
                                .build();

                        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                .setCollectionName(FirestoreRequest.USERS_COLLECTION)
                                .setParams(MapForm.convertObjectToMap(users))
                                .setEmail(username)
                                .setWhereFromField(FirestoreRequest.EMAIL_STRING)
                                .setWhereValueField(username)
                                .build();

                        request.findAll(body, new FirestoreListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                boolean isPresent = false;
                                if (any instanceof QuerySnapshot) {
                                    QuerySnapshot queryDocumentSnapshots = (QuerySnapshot) any;

                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.exists()) {
                                            Users c = documentSnapshot.toObject(Users.class);
                                            if (c != null) {
                                                c.setUserID(documentSnapshot.getId());
                                                new UserPref(GoogleLoginActivity.this).storeLogin(MapForm.convertObjectToMap(c));
                                                isPresent = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                pdLoading.dismiss();
                                if (isPresent) {
                                    Toast.makeText(GoogleLoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(GoogleLoginActivity.this, "Failed to login with google, please try again later", Toast.LENGTH_SHORT).show();
                                }
                                finish();
                            }

                            @Override
                            public void onError(Error error) {
                                createUser(body, users);
                            }
                        });

                    }
                } catch (ApiException e) {
                    pdLoading.dismiss();
                    Toast.makeText(GoogleLoginActivity.this, "Failed to login with google, please try again later", Toast.LENGTH_SHORT).show();
                    finish();
                }

            } else {
                pdLoading.dismiss();
                Toast.makeText(GoogleLoginActivity.this, "Failed to login with google, please try again later", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(beginSignInResult -> {
                    try {
                        oneTapLauncher.launch(new IntentSenderRequest.Builder(beginSignInResult.getPendingIntent().getIntentSender()).build());
                    } catch (Exception e) {
                        Log.e("oneTapClientFail", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                    }
                })
                .addOnFailureListener(e -> Log.e("oneTapClientFail", e.getLocalizedMessage()));
    }

    private void createUser(FirestoreRequestBody body, Users users) {
        request.insertUniqueData(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                pdLoading.dismiss();
                if (any instanceof String) {
                    String docID = (String) any;
                    users.setUserID(docID);
                    new UserPref(GoogleLoginActivity.this).storeLogin(MapForm.convertObjectToMap(users));
                    Toast.makeText(GoogleLoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(Error error) {
                pdLoading.dismiss();
                Toast.makeText(GoogleLoginActivity.this, "Failed To Create Account, Please Try Again Later", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


}
