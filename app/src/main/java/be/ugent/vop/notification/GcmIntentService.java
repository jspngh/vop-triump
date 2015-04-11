package be.ugent.vop.notification;

import android.app.IntentService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import be.ugent.vop.R;
import be.ugent.vop.feedback.Feedback;
import be.ugent.vop.ui.event.EventActivity;
import be.ugent.vop.ui.main.MainActivity;



/**
 * Created by vincent on 02/04/15.
 */
public class GcmIntentService extends IntentService {
    public static final String TAG = "GcmIntentService";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent in) {
        Bundle extras = in.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(in);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type.
             */
            String message = "no message";
            Intent intent= new Intent(this, MainActivity.class);

            switch(messageType){
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    message = "Send error: " + extras.toString();
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    message = "Deleted messages on server: " + extras.toString();
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:

                    switch(extras.getString(NotificationConstants.NOTIFICATION_TYPE)){
                        case NotificationConstants.TYPE_END_EVENT_TOP_RANKING:
                            message = extras.getString(getString(R.string.notification_top_ranking_event));
                            intent= new Intent(this, MainActivity.class);
                            break;
                        case NotificationConstants.TYPE_FEEDBACK:
                            message = getString(R.string.notification_feedback);
                            intent= new Intent(this, MainActivity.class);
                            intent.putExtra(Feedback.GIVE_FEEDBACK, true);
                            break;

                        default:
                            intent= new Intent(this, MainActivity.class);
                            break;
                    }
                    break;
                default:
                    intent= new Intent(this, MainActivity.class);
                    break;

            }

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    intent, 0);
            // Post notification
            sendNotification(message, pendingIntent);

            Log.i(TAG, "Send notification: " + message);
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(in);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg,PendingIntent contentIntent ) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.checkin_image)
                        .setContentTitle(getString(R.string.notification_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
