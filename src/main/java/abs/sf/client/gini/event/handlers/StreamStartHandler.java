package abs.sf.client.gini.event.handlers;

import abs.ixi.client.DeviceType;
import abs.ixi.client.PushNotificationService;
import abs.ixi.client.core.Platform;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.util.StringUtils;
import abs.sf.client.gini.managers.AndroidUserManager;
import abs.sf.client.gini.utils.SharedPrefProxy;


/**
 * {@link EventHandler} implementation to handle {@link Event.EventType#STREAM_START}
 * AND {@link Event.EventType#STREAM_RESTART}
 */
public class StreamStartHandler implements EventHandler {
    @Override
    public void handle(Event event) {
        updateDeviceToken();
        sendGetRosterRequest();
        sendChatRoomRequest();
        updateUserProfileData();
    }

    private void updateUserProfileData() {
        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
        userManager.reloadUserData();
    }

    private void updateDeviceToken() {
        PushNotificationService notificationSrevice = SharedPrefProxy.getInstance().getNotificationService();

        if (notificationSrevice != null) {
            String deviceToken = SharedPrefProxy.getInstance().getDeviceToken();

            if (!StringUtils.isNullOrEmpty(deviceToken)) {
                Platform.getInstance().getUserManager().updateDeviceToken(deviceToken, notificationSrevice, DeviceType.ANDROID);
            }
        }
    }

    private void sendGetRosterRequest() {
        int prevRosterVersion = SharedPrefProxy.getInstance().getRosterVersion();
        Platform.getInstance().getUserManager().sendGetRosterRequest(prevRosterVersion);
    }

    private void sendChatRoomRequest() {
        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
        userManager.sendGetChatRoomListRequest();
    }

}
