package abs.sf.client.genie.event.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Platform;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.EventHandler;
import abs.sf.client.genie.managers.AppUserManager;
import abs.sf.client.genie.utils.SFSDKProperties;

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
		AppUserManager userManager = (AppUserManager) Platform.getInstance().getUserManager();
		userManager.reloadUserData();
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
