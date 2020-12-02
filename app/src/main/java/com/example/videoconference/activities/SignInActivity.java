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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonSignIn;
    private ProgressBar signInProgressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        editTextEmail = findViewById(R.id.inputEmail);
        editTextPassword = findViewById(R.id.inputPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        signInProgressBar = findViewById(R.id.signInProgressBar);
    }
    public void onClickSignUp(View v)
    {
            startActivity(new Intent(this, SignUpActivity.class));
    }
    public void onClickSignIn(View v)
    {
        if(editTextEmail.getText().toString().trim().isEmpty())
        {
            editTextEmail.setError("Please enter your email");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString()).matches())
        {
            editTextEmail.setError("Please enter valid email address");
        }
        else if(editTextPassword.getText().toString().isEmpty())
        {
            editTextPassword.setError("Please enter this field");
        }
        else
        {
            signIn();
        }
    }

    private void signIn()
    {
            buttonSignIn.setVisibility(View.INVISIBLE);
            signInProgressBar.setVisibility(View.VISIBLE);

            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL,editTextEmail.getText().toString().trim())
                    .whereEqualTo(Constants.KEY_PASSWORD,editTextPassword.getText().toString().trim())
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()&&task.getResult()!=null&&task.getResult().getDocuments().size()>0)
                        {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                            preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else{
                            signInProgressBar.setVisibility(View.INVISIBLE);
                            buttonSignIn.setVisibility(View.VISIBLE);
                            Toast.makeText(SignInActivity.this, "Please check your email/password", Toast.LENGTH_SHORT).show();
                        }
                    });
    }
}
