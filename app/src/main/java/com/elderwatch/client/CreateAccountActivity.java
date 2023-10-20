package com.elderwatch.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityCreateAccountBinding;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;

public class CreateAccountActivity extends AppCompatActivity {

    ActivityCreateAccountBinding binding;

    FSRequest request;

    ProgressDialog pdLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        request = new FSRequest();
        pdLoading = new ProgressDialog(CreateAccountActivity.this);
        pdLoading.setMessage("Sending Request ...");
        pdLoading.setCancelable(false);
        setListeners();
    }

    private void setListeners() {
        binding.btnCreateAccount.setOnClickListener(v -> {
            String email = binding.editEmail.getText().toString();
            String firstName = binding.editFirstName.getText().toString();
            String middleName = binding.editMiddleName.getText().toString();
            String lastName = binding.editLastName.getText().toString();
            String birthDate = binding.editBirthDate.getText().toString();
            String phoneNumber = binding.editPhoneNumber.getText().toString();
            String address = binding.editAddress.getText().toString();
            String password = binding.editPassword.getText().toString();
            String confirmPassword = binding.editConfirmPassword.getText().toString();

            if (email.equals("")
                    || firstName.equals("")
                    || lastName.equals("")
                    || birthDate.equals("")
                    || phoneNumber.equals("")
                    || address.equals("")
                    || password.equals("")
                    || confirmPassword.equals("")
            ) {
                Toast.makeText(CreateAccountActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                if (confirmPassword.equals(password)) {
                    pdLoading.show();
                    Users users = new Users.UserBuilder()
                            .setEmail(email)
                            .setFirstName(firstName)
                            .setMiddleName(middleName)
                            .setLastName(lastName)
                            .setAddress(address)
                            .setBirthDate(birthDate)
                            .setPhoneNumber(phoneNumber)
                            .setPassword(password)
                            .setUserType(2)
                            .build();

                    FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                            .setParams(MapForm.convertObjectToMap(users))
                            .setCollectionName(FirestoreRequest.USERS_COLLECTION)
                            .build();

                    request.insertUniqueData(body, new FirestoreListener() {
                        @Override
                        public <T> void onSuccess(T any) {
                            pdLoading.dismiss();
                            Toast.makeText(CreateAccountActivity.this, "Successfully Created Account", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(Error error) {
                            pdLoading.dismiss();
                            Toast.makeText(CreateAccountActivity.this, "Failed To Create Account, Please Try Again Later", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(CreateAccountActivity.this, "Passwords Doesn't Match, Please Check Thoroughly", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
