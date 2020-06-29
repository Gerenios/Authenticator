package com.gerenios.aadinternals.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import com.azure.authenticator.R;


public class MessageReceiver extends FirebaseMessagingService {

    private static final String TAG = "AADInt Authenticator";
    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManager;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Log.d(TAG, "Message received!");

        // From should be MS production FCM 275572744697
        String from = remoteMessage.getFrom();

        // Extract the data from the message
        Map data = remoteMessage.getData();
        Bundle bundle = new Bundle();

        for (Map.Entry next : remoteMessage.getData().entrySet()) {
            bundle.putString((String) next.getKey(), (String) next.getValue());
        }

        // Accept the authentication request
        boolean success = acceptAuthRequest(bundle);
        final String message = String.format("Request ID: %s Success:%b",bundle.getString("guid"),success);

        // Toast
        /*Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        */

        // Initialize notification builder and manager
        if(this.builder == null) {
            this.builder = new NotificationCompat.Builder(this, getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(message)
                    .setContentText(String.format("User Object Id (SHA256): %s", bundle.getString("userObjectId")))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            this.notificationManager = NotificationManagerCompat.from(getApplicationContext());
        }
        // Show the notification
        notificationManager.notify(new Random().nextInt(9999999), builder.build());
    }

    /**
     * Sends an accept message
     * @param authInfo
     */
    private boolean acceptAuthRequest(Bundle authInfo)
    {
        // Get the token from the preferences
        Context context = getApplicationContext();
        SharedPreferences pref = context.getSharedPreferences("AADINTERNALS",Context.MODE_PRIVATE);
        String token = pref.getString("token","");

        // Get the required info from the message
        String url = String.format("https://%s",authInfo.getString("url"));
        String guid = authInfo.getString("guid");
        // Generate request id
        String requestId = UUID.randomUUID().toString();
        // Calculate an oath counter
        long oathCounter = System.currentTimeMillis()/1000/30;

        // Generate the body
        String body = String.format("<pfpMessage version=\"1.6\"><header><source><component type=\"pfsvc\" role=\"master\"><host ip=\"\" hostname=\"\" serverId=\"\"/></component></source></header><request request-id=\"%1$s\" async=\"0\" response-url=\"\" language=\"en\"><phoneAppAuthenticationResultRequest><phoneAppContext><guid>%2$s</guid><oathCode/><needDosPreventer>no</needDosPreventer><deviceToken>%3$s</deviceToken><version>6.2001.0140</version><osVersion>8.1.0</osVersion></phoneAppContext><authenticationResult>1</authenticationResult><newDeviceToken notificationType=\"gcm\">%3$s</newDeviceToken><oathCounter>53103312</oathCounter></phoneAppAuthenticationResultRequest></request></pfpMessage>", requestId, guid, token, oathCounter);

        String response=null;
        // Send the acceptance message
        try {
            URL responseUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) responseUrl.openConnection();
            try
            {
                // Send the request
                connection.setDoOutput(true);
                OutputStream out=new BufferedOutputStream(connection.getOutputStream());
                out.write(body.getBytes());
                out.flush();

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while((line = in.readLine()) != null)
                {
                    sb.append(line);
                }
                response = sb.toString();

            } catch (Exception e) {}
            finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check the response and return
        return response.contains("<authenticationResultResult>1<");
    }
    /**
     * Get's the token, aka initialises the app
     * @param context
     */
    public static void getToken(Context context)
    {
        try {

            // Init the the token, accept MS production FCM and MSA senders
            String token = FirebaseInstanceId.getInstance().getToken("275572744697,581753172647", "FCM");

            // Store to preferences
            SharedPreferences pref = context.getSharedPreferences("AADINTERNALS",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("token", token);
            editor.commit();

            //Log.d(TAG, "Token set to preferences");
        }
        catch (java.io.IOException e) {
            String message = e.getMessage();
        };

    }
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        // Get the new token with correct senders
        Context context = getApplicationContext();
        getToken(context);

        // Log'n Toast
        //Log.d(TAG, "Token changed!");

    }

}