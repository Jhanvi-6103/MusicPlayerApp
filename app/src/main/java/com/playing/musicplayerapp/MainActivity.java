package com.playing.musicplayerapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView songTitle, songDuration;
    private ImageView albumArt;
    private SeekBar songProgress;

    // Arrays for songs and album art
    private int[] songResources = {
            R.raw.alagasaman, // Replace with actual song files in res/raw
            R.raw.nightchanges,
            R.raw.atmyworst,
            R.raw.namonamo,
            R.raw.engofbeginning,
            R.raw.girlslikeu
    };

    private int[] albumArtResources = {
            R.drawable.alagasaman, // Replace with actual drawable resources
            R.drawable.nightchanges,
            R.drawable.amw,
            R.drawable.namonamo,
            R.drawable.eob,
            R.drawable.glu,
    };

    private String[] songTitles = {
            "Alag Aasaman",
            "Night Changes",
            "At my Worst",
            "Namo Namo",
            "End of Beginning",
            "Girls Like you"
    };

    private int currentSongIndex = 0;
    private Handler handler = new Handler();

    // Runnable to update SeekBar and song duration
    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                songProgress.setProgress(currentPosition);
                songDuration.setText(formatTime(currentPosition / 1000)); // Show time in mm:ss
                handler.postDelayed(this, 100); // Update every 100ms
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        songTitle = findViewById(R.id.songTitle);
        songDuration = findViewById(R.id.songDuration);
        albumArt = findViewById(R.id.albumArt);
        songProgress = findViewById(R.id.songProgress); // Initialize the SeekBar
        ImageButton pauseButton = findViewById(R.id.pauseButton);
        ImageButton nextButton = findViewById(R.id.nextButton);
        ImageButton rewindButton = findViewById(R.id.rewindButton);
        ImageButton decreaseButton = findViewById(R.id.decreaseButton);  // Decrease 10 seconds button
        ImageButton increaseButton = findViewById(R.id.increaseButton);  // Increase 10 seconds button

        // Set up MediaPlayer for the first song
        setupMediaPlayer(currentSongIndex);

        // Show Toasts for each button
        showToastForButton(pauseButton, "Pause/Resume");
        showToastForButton(nextButton, "Next");
        showToastForButton(rewindButton, "Rewind");
        showToastForButton(decreaseButton, "Decrease 10 seconds");
        showToastForButton(increaseButton, "Increase 10 seconds");

        // Pause/Resume Button (toggles between pause and play)
        pauseButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                pauseButton.setImageResource(R.drawable.play); // Change icon to Play
                handler.removeCallbacks(updateSeekBarRunnable); // Stop updating SeekBar when paused
            } else {
                mediaPlayer.start();
                pauseButton.setImageResource(R.drawable.pause); // Change icon to Pause
                handler.post(updateSeekBarRunnable); // Resume updating SeekBar
            }
        });

        // Rewind Button (Play previous song)
        rewindButton.setOnClickListener(v -> playPreviousSong());

        // Next Button (Play next song)
        nextButton.setOnClickListener(v -> playNextSong());

        // Decrease 10 seconds Button
        decreaseButton.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int newPosition = Math.max(currentPosition - 10000, 0); // Decrease 10 seconds, ensuring it's not negative
            mediaPlayer.seekTo(newPosition);
            songProgress.setProgress(newPosition); // Update SeekBar
            songDuration.setText(formatTime(newPosition / 1000)); // Update duration display
        });

        // Increase 10 seconds Button
        increaseButton.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int newPosition = Math.min(currentPosition + 10000, mediaPlayer.getDuration()); // Increase 10 seconds, ensuring it's within song duration
            mediaPlayer.seekTo(newPosition);
            songProgress.setProgress(newPosition); // Update SeekBar
            songDuration.setText(formatTime(newPosition / 1000)); // Update duration display
        });

        // Set the SeekBar's listener for user interaction
        songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress); // Seek to the new position
                    songDuration.setText(formatTime(progress / 1000)); // Update the song duration display
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Handle when user starts dragging SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Handle when user stops dragging SeekBar
            }
        });
    }

    private void showToastForButton(View button, String message) {
        button.setOnLongClickListener(v -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            return true;  // Return true to indicate the long press was handled
        });
    }

    private void setupMediaPlayer(int songIndex) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Release the current MediaPlayer
        }

        // Set MediaPlayer with the new song
        mediaPlayer = MediaPlayer.create(this, songResources[songIndex]);

        mediaPlayer.setOnPreparedListener(mp -> {
            songProgress.setMax(mediaPlayer.getDuration()); // Set SeekBar max to song duration
            songProgress.setProgress(0); // Reset SeekBar to start (0)
            songDuration.setText("00:00"); // Reset duration display
            mediaPlayer.start(); // Start playing after prepared
            handler.post(updateSeekBarRunnable); // Start updating SeekBar
        });

        mediaPlayer.setOnCompletionListener(mp -> playNextSong());

        // Update UI elements for the current song
        songTitle.setText(songTitles[songIndex]);
        albumArt.setImageResource(albumArtResources[songIndex]);
    }

    private void playNextSong() {
        currentSongIndex = (currentSongIndex + 1) % songResources.length; // Loop back to first song
        setupMediaPlayer(currentSongIndex); // Set up new song
    }

    private void playPreviousSong() {
        currentSongIndex = (currentSongIndex - 1 + songResources.length) % songResources.length; // Loop back to last song
        setupMediaPlayer(currentSongIndex); // Set up previous song
    }

    // Convert time in seconds to mm:ss format
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacks(updateSeekBarRunnable); // Stop SeekBar updates when activity is destroyed
    }
}
