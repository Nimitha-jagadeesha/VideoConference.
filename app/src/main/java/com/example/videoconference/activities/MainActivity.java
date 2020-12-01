package com.example.videoconference.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoconference.R;
import com.example.videoconference.adaptors.UsersAdaptors;
import com.example.videoconference.listners.UsersListeners;
import com.example.videoconference.models.User;
import com.example.videoconference.utilities.Constants;
import com.example.videoconference.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListeners {

    private PreferenceManager preferenceManager;
    private List<User> users;
    private UsersAdaptors usersAdaptors;
    private TextView textViewErrorMessage;
    private ProgressBar usersProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager =new PreferenceManager(getApplicationContext());

        TextView textViewTitle = findViewById(R.id.textTitle);
        textViewTitle.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));
        findViewById(R.id.textSignOut).setOnClickListener(v -> signOut());


        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful()&&task.getResult()!=null)
            {
                sendFCMTokenToDatabase(task.getResult().getToken());
            }
        });
        RecyclerView useRecyclerView = findViewById(R.id.usersRecyclerView);
        textViewErrorMessage = findViewById(R.id.textErrorMessage);
        usersProgressBar = findViewById(R.id.userProgressBar);
        users= new ArrayList<>();
        usersAdaptors= new UsersAdaptors(users);
        useRecyclerView.setAdapter(usersAdaptors);

        getUsers();
    }

    private void getUsers()
    {
        usersProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    usersProgressBar.setVisibility(View.GONE);
                    String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful()&&task.getResult()!=null)
                    {
                            for(QueryDocumentSnapshot documentSnapshot: task.getResult())
                            {
                                if(myUserId.equals(documentSnapshot.getId()))
                                {
                                    continue;
                                }
                                User user = new User();
                                user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                                user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                                user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                                user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                users.add(user);
                            }
                            if(users.size()>0)
                            {
                                usersAdaptors.notifyDataSetChanged();
                            }
                            else
                            {
                                textViewErrorMessage.setText(String.format("%s","No users available"));
                                textViewErrorMessage.setVisibility(View.VISIBLE);
                            }
                    }
                    else{
                        textViewErrorMessage.setText(String.format("%s","No users available"));
                        textViewErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendFCMTokenToDatabase(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
//                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this,"Token Updated Successfully",Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this,"Unable to send token : "+ e.getMessage(),Toast.LENGTH_LONG).show());
    }
    private void signOut(){
        Toast.makeText(this, "Signing Out", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String,Object>updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreference();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to sign out", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void initiateVideoMeeting(User user) {
        
    }

    @Override
    public void initiateAudioMeeting(User user) {

    }
}
