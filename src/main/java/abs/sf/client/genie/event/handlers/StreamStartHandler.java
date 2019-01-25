package abs.sf.client.genie.event.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.DeviceType;
import abs.ixi.client.Platform;
import abs.ixi.client.PushNotificationService;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.util.StringUtils;
import abs.sf.client.genie.managers.AppUserManager;
import abs.sf.client.genie.utils.SFSDKProperties;

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
		AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();
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
		AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();
		userManager.sendGetChatRoomListRequest();
	}

}
