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
import com.example.videoconference.models.User;
import com.example.videoconference.network.ApiClient;
import com.example.videoconference.network.ApiService;
import com.example.videoconference.utilities.Constants;
import com.example.videoconference.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingRoom = null;
    private String meetingType = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        preferenceManager = new PreferenceManager(getApplicationContext());


        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        meetingType = getIntent().getStringExtra("type");

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
        imageViewStopInvitation.setOnClickListener(v -> {
            if(user!= null){
                cancelInvitationResponse(user.token);
                finish();
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful()&&task.getResult()!=null){
                    inviterToken = task.getResult().getToken();
                    if(meetingType!=null && user!=null)
                    {
                        initiateMeeting(meetingType, user.token);
                    }
                }
            }
        });


    }

    private void initiateMeeting(String meetingType, String receiverToken){

        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME,preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL,preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN,inviterToken);

            meetingRoom =
                    preferenceManager.getString(Constants.KEY_USER_ID)+"_"+
                            UUID.randomUUID().toString().substring(0,5);

            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendRemoteInvitation(body.toString(), Constants.REMOTE_MSG_INVITATION);
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void sendRemoteInvitation(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessagingHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull  Call<String> call,@NonNull Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation Sent Successfully", Toast.LENGTH_SHORT).show();
                    }else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
                else{
                    Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private void cancelInvitationResponse(String receiverToken)
    {
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteInvitation(body.toString(), Constants.REMOTE_MSG_INVITATION_CANCELLED);
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type!=null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                   try {
                       URL serverURL = new URL("https://meet.jit.si");
                       JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                       builder.setServerURL(serverURL);
                       builder.setWelcomePageEnabled(false);
                       builder.setRoom(meetingRoom);
                       if(meetingType.equals("audio")){
                           builder.setVideoMuted(true);
                       }
                       JitsiMeetActivity.launch(OutgoingInvitationActivity.this,builder.build());
                       finish();
                   }catch (Exception e){
                       Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
                }else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
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