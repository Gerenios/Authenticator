package com.gerenios.aadinternals.authenticator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.azure.authenticator.R;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the token
        new Thread( new Runnable() {
            @Override
            public void run() {
                MessageReceiver.getToken(getApplicationContext());
            }
        }).start();

        // Set the token to the text box
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                copyToken();
            }
        }, 200);

        createNotificationChannel();
    }

    // Copies the token to clipboard
    public void copyToken() {

        // Get the token from the preferences
        Context context = getApplicationContext();
        SharedPreferences pref = context.getSharedPreferences("AADINTERNALS",Context.MODE_PRIVATE);
        String token = pref.getString("token","");

        EditText editText = (EditText)findViewById(R.id.token_title);
        editText.setText(token);
        // Copy to clipboard
        //ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        //ClipData clip = android.content.ClipData.newPlainText("Authenticator token",token);
        //clipboard.setPrimaryClip(clip);


        // Log'n Toast
        //Log.d(TAG, "Token copied to clipboard");
        Toast.makeText(MainActivity.this, "Token copied to clipboard", Toast.LENGTH_SHORT).show();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}