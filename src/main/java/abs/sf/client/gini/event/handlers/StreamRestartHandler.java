package abs.sf.client.gini.event.handlers;

import abs.ixi.client.core.Platform;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.EventHandler;
import abs.sf.client.gini.managers.AndroidUserManager;
import abs.sf.client.gini.utils.SharedPrefProxy;

public class StreamRestartHandler implements EventHandler{
    @Override
    public void handle(Event e) {
        sendGetRosterRequest();
        sendChatRoomRequest();
        updateUserProfileData();
    }

    private void updateUserProfileData() {
        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
        userManager.reloadUserData();
    }

    private void sendGetRosterRequest() {
        int prevRosterVersion =  SharedPrefProxy.getInstance().getRosterVersion();
        Platform.getInstance().getUserManager().sendGetRosterRequest(prevRosterVersion);
    }

    private void sendChatRoomRequest() {
        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
        userManager.sendGetChatRoomListRequest();
    }
}
