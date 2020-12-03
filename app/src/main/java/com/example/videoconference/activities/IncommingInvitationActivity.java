package com.example.videoconference.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoconference.R;
import com.example.videoconference.network.ApiClient;
import com.example.videoconference.network.ApiService;
import com.example.videoconference.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        ImageView imageViewAcceptInvitation = findViewById(R.id.imageAcceptInvitation);
        imageViewAcceptInvitation.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));
        ImageView imageViewRejection = findViewById(R.id.imageRejectInvitation);
        imageViewRejection.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));
    }
    private void sendInvitationResponse(String type, String receiverToken)
    {
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessagingHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                        if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                            Toast.makeText(IncommingInvitationActivity.this, "Invitation Accepted", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(IncommingInvitationActivity.this, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        }
                }
                else{
                    Toast.makeText(IncommingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(IncommingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null) {
                if(type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}