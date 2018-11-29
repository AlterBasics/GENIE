package abs.sf.client.gini.event.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.DeviceType;
import abs.ixi.client.PushNotificationService;
import abs.ixi.client.core.Platform;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.util.StringUtils;
import abs.sf.client.gini.managers.AndroidUserManager;
import abs.sf.client.gini.utils.SFSDKProperties;

/**
 * {@link EventHandler} implementation to handle
 * {@link Event.EventType#STREAM_START} AND
 * {@link Event.EventType#STREAM_RESTART}
 */
public class StreamStartHandler implements EventHandler {
	private static final Logger LOGGER = Logger.getLogger(StreamStartHandler.class.getName());

	@Override
	public void handle(Event event) {
		try {
			updateDeviceToken();
			sendGetRosterRequest();
			sendChatRoomRequest();
			updateUserProfileData();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to handle Stream start Event due to " + e.getMessage(), e);
		}
	}

	private void updateUserProfileData() {
		AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
		userManager.reloadUserData();
	}

	private void updateDeviceToken() throws Exception {
		PushNotificationService notificationSrevice = SFSDKProperties.getInstance().getNotificationService();

		if (notificationSrevice != null) {
			String deviceToken = SFSDKProperties.getInstance().getDeviceToken();

			if (!StringUtils.isNullOrEmpty(deviceToken)) {
				Platform.getInstance().getUserManager().updateDeviceToken(deviceToken, notificationSrevice,
						DeviceType.ANDROID);
			}
		}
	}

	private void sendGetRosterRequest() throws Exception {
		int prevRosterVersion = SFSDKProperties.getInstance().getRosterVersion();
		Platform.getInstance().getUserManager().sendGetRosterRequest(prevRosterVersion);
	}

	private void sendChatRoomRequest() {
		AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
		userManager.sendGetChatRoomListRequest();
	}

}
