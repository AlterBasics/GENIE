package abs.sf.client.gini.utils;

import java.util.logging.Logger;

import abs.ixi.client.ChatManager;
import abs.ixi.client.PresenceManager;
import abs.ixi.client.UserManager;
import abs.ixi.client.core.Platform;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.io.UndeliveredStanzaManager;
import abs.ixi.client.io.XMPPStreamManager;
import abs.ixi.client.net.ConnectionConfig;
import abs.ixi.client.net.ConnectionManager;
import abs.ixi.client.net.XMPPConnection;
import abs.sf.client.android.db.DbManager;
import abs.sf.client.android.event.handlers.ChatRoomReceiveHandler;
import abs.sf.client.android.event.handlers.RosterReceiveHandler;
import abs.sf.client.android.event.handlers.RosterUpdateHandler;
import abs.sf.client.android.event.handlers.StreamRestartHandler;
import abs.sf.client.android.event.handlers.StreamStartHandler;
import abs.sf.client.android.managers.AndroidChatManager;
import abs.sf.client.android.managers.AndroidPresenceManager;
import abs.sf.client.android.managers.AndroidUserManager;

/**
 * SDKLoader loads Stringflow SDK into memory. As part of the sdk loading,
 * sdk core objects are created, db tables are updated, Packet Reader and
 * Writer threads are set up and user level classes such as
 * {@link PresenceManager}, {@link ChatManager}, {@link UserManager} etc are
 * instantiated and stored inside {@link abs.ixi.client.core.Platform} object
 */
public class SDKLoader {
    private static final Logger LOGGER = Logger.getLogger(SDKLoader.class.getName());
    private static boolean isSDKLoaded;
    private static boolean loadingSDK;


    /**
     * load SDK into memory. The method takes server ip, port and {@link ContextProvider}
     * instance in argument.
     *
     * @param xmppServerIP   xmpp server ip address
     * @param xmppServerPort     xmpp server port
     * @param mediaServerIP   media server ip address
     * @param mediaServerPort     media server port
     * @param provider {@link ContextProvider} instance. This is injected into SDK by Application
     */
    public static void loadSDK(String xmppServerIP, int xmppServerPort, String mediaServerIP, int mediaServerPort, ContextProvider provider) {
        if (isSDKLoaded || loadingSDK) {
            while (loadingSDK) ;

        } else {
            sdkLoadingStarted();
            ConnectionManager.instantiate(xmppServerIP, xmppServerPort, mediaServerIP, mediaServerPort);
            XMPPConnection con = ConnectionManager.getInstance().getXmppConnection();

            LOGGER.info("initializing SDK objects...");
            initializeSDKObjects(con, provider);
            sdkLoaded();
        }
    }

    /**
     * load SDK into memory. The method takes server ip, port and {@link ContextProvider}
     * instance in argument.
     *
     * @param xmppConnectionconfig
     * @param mimeConnectionconfig
     * @param provider
     */
    public static void loadSDK(ConnectionConfig xmppConnectionconfig, ConnectionConfig mimeConnectionconfig, ContextProvider provider) {
        if (isSDKLoaded || loadingSDK) {
            while (loadingSDK) ;

        } else {
            sdkLoadingStarted();
            ConnectionManager.instantiate(xmppConnectionconfig, mimeConnectionconfig);
            XMPPConnection con = ConnectionManager.getInstance().getXmppConnection();

            LOGGER.info("initializing SDK objects...");
            initializeSDKObjects(con, provider);
            sdkLoaded();
        }
    }

    private static void sdkLoadingStarted() {
        loadingSDK = true;
    }

    private static void sdkLoaded() {
        isSDKLoaded = true;
        loadingSDK = false;
    }

    private static void initializeSDKObjects(XMPPConnection connection, ContextProvider provider) {
        Platform.getInstance().getSession().put(ContextProvider.KEY_CONTEXT, provider);

        setupUndeliveredStanzaManager();

        XMPPStreamManager streamManager = new XMPPStreamManager(connection);

        Platform.getInstance().addChatManager(new AndroidChatManager(streamManager));
        Platform.getInstance().addUserManager(new AndroidUserManager(streamManager));
        Platform.getInstance().addPresenceManager(new AndroidPresenceManager(streamManager));

        addEventListeners();
    }

    private static void setupUndeliveredStanzaManager() {
        UndeliveredStanzaManager undeliveredStanzaManager = new UndeliveredStanzaManager(new UndeliverStanzaPersistenceMechanism());
        Platform.getInstance().addUndeliveredStanzaManager(undeliveredStanzaManager);
    }

    /**
     * Add core event handlers to {@link abs.ixi.client.core.event.EventBus}.
     * Applications can further add/remove event handlers and listeners from
     * event bus using {@link Platform}.
     */
    private static void addEventListeners() {
        LOGGER.info("Adding event handlers and listeners");

        LOGGER.info("Adding RosterReceive Handler");
        Platform.addEventHandler(Event.EventType.ROSTER_RECEIVE, new RosterReceiveHandler());

        LOGGER.info("Adding RosterUpdate Handler");
        Platform.addEventHandler(Event.EventType.ROSTER_UPDATE, new RosterUpdateHandler());

        LOGGER.info("Adding ChatRoomReceive Handler");
        Platform.addEventHandler(Event.EventType.CHAT_ROOM_RECEIVE, new ChatRoomReceiveHandler());

        LOGGER.info("Adding Stream Start Handler");
        Platform.addEventHandler(Event.EventType.STREAM_START, new StreamStartHandler());

        LOGGER.info("Adding Stream Restart Handler");
        Platform.addEventHandler(Event.EventType.STREAM_RESTART, new StreamRestartHandler());

        LOGGER.info("Adding Server Ack Handler");
        AndroidChatManager chatManager = (AndroidChatManager) Platform.getInstance().getChatManager();
        Platform.addEventHandler(Event.EventType.MESSAGE_DELIVERED, chatManager.new MessageAckHandler());
    }

    public static void unloadSdk() {
        DbManager.getInstance().clearDatabase();
        SharedPrefProxy.getInstance().setRosterVersion(0);
        isSDKLoaded = false;
    }
}
