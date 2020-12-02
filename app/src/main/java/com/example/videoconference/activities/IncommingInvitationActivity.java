package com.example.videoconference.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoconference.R;
import com.example.videoconference.utilities.Constants;

public class IncommingInvitationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomming_invitation);

        ImageView imageViewMeetingType = findViewById(R.id.imageMeetingType);
        String meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if (meetingType!= null)
        {
            if(meetingType.equals("video"))
                imageViewMeetingType.setImageResource(R.drawable.ic_video);
        }

        TextView textViewFirstChar = findViewById(R.id.textFirstChar);
        TextView textViewUserName = findViewById(R.id.textUserName);
        TextView textViewEmail = findViewById(R.id.textEmail);

        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstName!=null){
            textViewFirstChar.setText(firstName.substring(0,1));
        }
        textViewUserName.setText(String.format("%s %s",firstName, getIntent().getStringExtra(Constants.KEY_LAST_NAME)));
        textViewEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));
    }
}