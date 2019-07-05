package com.remindfeedbackproject.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.remindfeedbackproject.R;

import java.io.File;

public class VideoPlayActivity extends AppCompatActivity {
    private VideoView videoView;
    private MediaController mediaController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoplay_activity);

        videoView = (VideoView) findViewById(R.id.videoplay_videoview);

        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                mediaController.show();
            }
        });

        //fragment에서 인텐트 값 가져옴
        Intent intent = getIntent();
        String[] videoData = intent.getStringArrayExtra("intentData");

        //fragment에서 경로 얻어옴
        videoView.setVideoPath(videoData[2]);
    }
}
