package com.example.videoconference.listners;

import com.example.videoconference.models.User;

public interface UsersListeners {
    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);
}
