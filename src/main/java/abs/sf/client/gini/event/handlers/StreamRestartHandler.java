package abs.sf.client.gini.event.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Platform;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.EventHandler;
import abs.sf.client.gini.managers.AndroidUserManager;
import abs.sf.client.gini.utils.SFSDKProperties;

public class StreamRestartHandler implements EventHandler {
	private static final Logger LOGGER = Logger.getLogger(StreamRestartHandler.class.getName());

	@Override
	public void handle(Event event) {
		try {
			sendGetRosterRequest();
			sendChatRoomRequest();
			updateUserProfileData();

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to handle Stream Restart Event due to " + e.getMessage(), e);
		}
	}

	private void updateUserProfileData() {
		AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
		userManager.reloadUserData();
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
