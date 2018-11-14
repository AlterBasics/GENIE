package abs.sf.client.gini.notification.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import abs.ixi.client.PushNotificationService;
import abs.sf.client.gini.utils.SharedPrefProxy;

public class SFFcmInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeToken(token);
    }

    public void handleTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeToken(token);
    }

    private void storeToken(String token) {
        SharedPrefProxy.getInstance().savePushNotifiactionDetatils(PushNotificationService.FCM, token);
    }
}
