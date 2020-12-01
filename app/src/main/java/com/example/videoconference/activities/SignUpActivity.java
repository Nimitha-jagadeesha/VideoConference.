package com.example.videoconference.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.videoconference.R;
import com.example.videoconference.utilities.Constants;
import com.example.videoconference.utilities.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPassword, editTextConfirmPassword, editTextEmail;
    private ProgressBar signUpProgressBar;
    private PreferenceManager preferenceManager;

    MaterialButton signUpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        preferenceManager = new PreferenceManager(getApplicationContext());

        editTextFirstName = findViewById(R.id.inputFirstName);
        editTextLastName = findViewById(R.id.inputLastName);
        editTextPassword = findViewById(R.id.inputPassword);
        editTextConfirmPassword = findViewById(R.id.inputConfirmPassword);
        editTextEmail = findViewById(R.id.inputEmail);
        signUpProgressBar = findViewById(R.id.signUpProgress);
        signUpButton = findViewById(R.id.buttonSignUp);
    }

    public void onClickSignIn(View v)
    {
        onBackPressed();
    }

    public void oClickImageBack(View v)
    {
        onBackPressed();
    }

    public void  onClickSignUp(View v)
    {
        if(editTextFirstName.getText().toString().isEmpty())
        {
            editTextFirstName.setError("Enter the first name");

        }
        else if(editTextLastName.getText().toString().trim().isEmpty())
        {
            editTextLastName.setError("Enter Last Name");
        }
        else if(editTextEmail.getText().toString().trim().isEmpty())
        {
            editTextEmail.setError("Enter this field");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString()).matches())
        {
            editTextEmail.setError("Please enter valid email address");
        }
        else if(editTextPassword.getText().toString().trim().isEmpty())
        {
            editTextPassword.setError("Enter Password");
        }
        else if(editTextConfirmPassword.getText().toString().trim().isEmpty())
        {
            editTextConfirmPassword.setError("Confirm your password");
        }
        else if(!editTextConfirmPassword.getText().toString().equals(editTextPassword.getText().toString()))
        {
            editTextConfirmPassword.setError("Confirm password must be same as password");
        }
        else
        {
            signUp();
        }
    }
    private void signUp(){
            signUpButton.setVisibility(View.INVISIBLE);
            signUpProgressBar.setVisibility(View.VISIBLE);

            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> user = new HashMap<>();
            user.put(Constants.KEY_FIRST_NAME, editTextFirstName.getText().toString().trim());
            user.put(Constants.KEY_LAST_NAME, editTextLastName.getText().toString().trim());
            user.put(Constants.KEY_EMAIL, editTextEmail.getText().toString().trim());
            user.put(Constants.KEY_PASSWORD, editTextPassword.getText().toString().trim());

            database.collection(Constants.KEY_COLLECTION_USERS)
                    .add(user)
                    .addOnSuccessListener(documentReference -> {
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, editTextFirstName.getText().toString().trim());
                        preferenceManager.putString(Constants.KEY_LAST_NAME, editTextLastName.getText().toString().trim());
                        preferenceManager.putString(Constants.KEY_EMAIL, editTextEmail.getText().toString().trim());

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    })
                    .addOnFailureListener(e -> {
                        signUpProgressBar.setVisibility(View.INVISIBLE);
                        signUpButton.setVisibility(View.VISIBLE);
                        Toast.makeText(SignUpActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
    }
}
