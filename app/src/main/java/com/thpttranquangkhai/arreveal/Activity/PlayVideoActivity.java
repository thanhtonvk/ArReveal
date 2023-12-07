package com.thpttranquangkhai.arreveal.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

public class PlayVideoActivity extends AppCompatActivity {
    MediaController mediaController;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        mediaController = new MediaController(PlayVideoActivity.this);
        videoView = findViewById(R.id.play_video);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(Constants.idYoutube);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.start();
    }
}