
package abs.sf.client.genie.utils;

import java.util.logging.Level;
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
import abs.sf.client.genie.db.DbManager;
import abs.sf.client.genie.db.exception.DbException;
import abs.sf.client.genie.event.handlers.ChatRoomReceiveHandler;
import abs.sf.client.genie.event.handlers.RosterReceiveHandler;
import abs.sf.client.genie.event.handlers.RosterUpdateHandler;
import abs.sf.client.genie.event.handlers.StreamRestartHandler;
import abs.sf.client.genie.event.handlers.StreamStartHandler;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.managers.AppChatManager;
import abs.sf.client.genie.managers.AppPresenceManager;
import abs.sf.client.genie.managers.AppUserManager;

/**
 * SDKLoader loads Stringflow SDK into memory. As part of the sdk loading, sdk
 * core objects are created, db tables are updated, Packet Reader and Writer
 * threads are set up and user level classes such as {@link PresenceManager},
 * {@link ChatManager}, {@link UserManager} etc are instantiated and stored
 * inside {@link abs.ixi.client.core.Platform} object
 */
public class SDKLoader {
	private static final Logger LOGGER = Logger.getLogger(SDKLoader.class.getName());

	private static boolean isSDKLoaded;
	private static boolean loadingSDK;

	/**
	 * load SDK into memory. The method takes server ip, port and
	 * {@link ContextProvider} instance in argument.
	 *
	 * @param xmppServerIP xmpp server ip address
	 * @param xmppServerPort xmpp server port
	 * @param mediaServerIP media server ip address
	 * @param mediaServerPort media server port
	 * @throws StringflowErrorException
	 */
	public static void loadSDK(String xmppServerIP, int xmppServerPort, String mediaServerIP, int mediaServerPort)
			throws StringflowErrorException {
		if (isSDKLoaded || loadingSDK) {
			while (loadingSDK)
				;

		} else {
			sdkLoadingStarted();
			ConnectionManager.instantiate(xmppServerIP, xmppServerPort, mediaServerIP, mediaServerPort);
			XMPPConnection con = ConnectionManager.getInstance().getXmppConnection();

			LOGGER.info("initializing SDK objects...");
			initializeSDKObjects(con);
			initilizeSDKDefaultProperties();
			sdkLoaded();
		}
	}

	/**
	 * load SDK into memory. The method takes server ip, port and
	 * {@link ContextProvider} instance in argument.
	 *
	 * @param xmppConnectionconfig
	 * @param mimeConnectionconfig
	 * @param provider
	 * @throws StringflowErrorException
	 */
	public static void loadSDK(ConnectionConfig xmppConnectionconfig, ConnectionConfig mimeConnectionconfig)
			throws StringflowErrorException {
		if (isSDKLoaded || loadingSDK) {
			while (loadingSDK)
				;

		} else {
			sdkLoadingStarted();
			ConnectionManager.instantiate(xmppConnectionconfig, mimeConnectionconfig);
			XMPPConnection con = ConnectionManager.getInstance().getXmppConnection();

			LOGGER.info("initializing SDK objects...");
			initializeSDKObjects(con);

			initilizeSDKDefaultProperties();
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

	private static void initializeSDKObjects(XMPPConnection connection) throws StringflowErrorException {
		setupUndeliveredStanzaManager();

		XMPPStreamManager streamManager = new XMPPStreamManager(connection);

		Platform.getInstance().addChatManager(new AppChatManager(streamManager));
		Platform.getInstance().addUserManager(new AppUserManager(streamManager));
		Platform.getInstance().addPresenceManager(new AppPresenceManager(streamManager));

		addEventListeners();
	}

	private static void initilizeSDKDefaultProperties() throws StringflowErrorException {
		SFSDKProperties.getInstance().enableChatMarkers();
		SFSDKProperties.getInstance().enableChatStateNotification();
	}

	private static void setupUndeliveredStanzaManager() {
		UndeliveredStanzaManager undeliveredStanzaManager = new UndeliveredStanzaManager(
				new UndeliverStanzaPersistenceMechanism());
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
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();
		Platform.addEventHandler(Event.EventType.MESSAGE_DELIVERED, chatManager.new MessageAckHandler());
	}

	public static void unloadSdk() throws StringflowErrorException {
		LOGGER.log(Level.FINE, "Unloading Stringflow Exception");
		try {

			DbManager.getInstance().cleanUpAllData();

		} catch (DbException e) {
			LOGGER.log(Level.INFO, "Failed to cleanup all db data during unloading SDK", e);
			e.printStackTrace();
			throw new StringflowErrorException("Failed to cleanup all db data during unloading SDK", e);
		}

		SFSDKProperties.getInstance().setRosterVersion(0);
		isSDKLoaded = false;
	}

	public static void createDatabaseSchema(final String h2DbFilePath) throws StringflowErrorException {
		LOGGER.log(Level.INFO, "Initiating db schema using db file path : " + h2DbFilePath);

		try {
			SFSDKProperties.getInstance().setH2DbFilePath(h2DbFilePath);
			DbManager.getInstance().createDatabaseSchema();

		} catch (StringflowErrorException e) {
			LOGGER.log(Level.WARNING, "Failed to create db schema", e);
			throw e;

		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to create db schema", e);
			throw new StringflowErrorException(e.getMessage(), e);
		}

	}

}
