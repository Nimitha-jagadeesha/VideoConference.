package com.example.videoconference.adaptors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.videoconference.R;
import com.example.videoconference.listners.UsersListeners;
import com.example.videoconference.models.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



public class UsersAdaptors extends RecyclerView.Adapter<UsersAdaptors.UserViewHolder>{

    private List<User> users;
    private UsersListeners usersListeners;

    public UsersAdaptors(List<User> users, UsersListeners usersListeners) {
        this.users = users;
        this.usersListeners = usersListeners;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.setUserData(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        TextView textFirstChar, textUsername, textEmail;
        ImageView imageAudioMeeting, imageVideoMeeting;

         UserViewHolder(@NonNull View itemView) {
            super(itemView);
             textFirstChar = itemView.findViewById(R.id.textFirstChar);
             textUsername = itemView.findViewById(R.id.textUserName);
             textEmail = itemView.findViewById(R.id.textEmail);
             imageAudioMeeting = itemView.findViewById(R.id.imageAudioMeeting);
             imageVideoMeeting = itemView.findViewById(R.id.imageVideoMeeting);

        }


        void setUserData(User user)
        {
            textFirstChar.setText(user.firstName.substring(0,1));
            textUsername.setText(String.format("%s %s",user.firstName, user.lastName));
            textEmail.setText(user.email);
            imageAudioMeeting.setOnClickListener(v -> usersListeners.initiateAudioMeeting(user));
            imageVideoMeeting.setOnClickListener(v -> usersListeners.initiateVideoMeeting(user));
        }
    }
}
