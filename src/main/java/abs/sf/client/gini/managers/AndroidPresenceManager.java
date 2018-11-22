package abs.sf.client.gini.managers;


import abs.ixi.client.PresenceManager;
import abs.ixi.client.core.Packet;
import abs.ixi.client.core.Platform;
import abs.ixi.client.io.XMPPStreamManager;
import abs.ixi.client.xmpp.packet.Presence;
import abs.sf.client.android.db.DbManager;

public class AndroidPresenceManager extends PresenceManager {

    public AndroidPresenceManager(XMPPStreamManager streamManager) {
        super(streamManager);
    }

    @Override
    public void collect(Packet packet) {
        forwardPacket(packet);

        if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            Presence.PresenceType type = presence.getType();

            if (! presence.isMuc() && (type == Presence.PresenceType.UNAVAILABLE
                    || type == Presence.PresenceType.AVAILABLE)) {
                DbManager.getInstance().addOrUpdatePresence(presence.getFrom().getBareJID(), type);
            }

            if(presence.isVCardUpdate()) {
                AndroidUserManager androidUserManager = (AndroidUserManager) Platform.getInstance().getUserManager();
                androidUserManager.reloadUserData(presence.getFrom());
            }
        }
    }
}
