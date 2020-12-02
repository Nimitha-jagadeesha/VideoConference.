package com.example.videoconference.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.videoconference.R;
import com.example.videoconference.models.User;
import com.example.videoconference.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private String inviteToken = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(task.isSuccessful()&&task.getResult()!=null){
                        inviteToken = task.getResult().getToken();
                    }
            }
        });

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        String meetingType = getIntent().getStringExtra("type");

        if(meetingType!= null)
        {
            if(meetingType.equals("video"))
            {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }

        }

        TextView textViewFirstChar = findViewById(R.id.textFirstChar);
        TextView textViewUsername = findViewById(R.id.textUserName);
        TextView textViewEmail = findViewById(R.id.textEmail);

        User user = (User) getIntent().getSerializableExtra("user");

        if(user!=null)
        {
            textViewFirstChar.setText(user.firstName.substring(0,1));
            textViewUsername.setText(String.format("%s %s",user.firstName,user.lastName));
            textViewEmail.setText(user.email);
        }
        ImageView imageViewStopInvitation = findViewById(R.id.imageStopInvitation);
        imageViewStopInvitation.setOnClickListener(v -> onBackPressed());
    }
}