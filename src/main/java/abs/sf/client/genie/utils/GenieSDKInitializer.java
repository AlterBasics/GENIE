
package abs.sf.client.genie.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.ChatManager;
import abs.ixi.client.JavaSDKInitializer;
import abs.ixi.client.Platform;
import abs.ixi.client.PresenceManager;
import abs.ixi.client.UserManager;
import abs.ixi.client.core.InitializationErrorException;
import abs.ixi.client.core.ResponseCorrelator;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.io.UndeliveredStanzaManager;
import abs.ixi.client.io.XMPPStreamManager;
import abs.ixi.client.net.ConnectionManager;
import abs.ixi.client.net.XMPPConnection;
import abs.sf.client.genie.db.DbManager;
import abs.sf.client.genie.db.exception.DbException;
import abs.sf.client.genie.event.handlers.ChatRoomReceiveHandler;
import abs.sf.client.genie.event.handlers.RosterReceiveHandler;
import abs.sf.client.genie.event.handlers.RosterUpdateHandler;
import abs.sf.client.genie.event.handlers.StreamRestartHandler;
import abs.sf.client.genie.event.handlers.StreamStartHandler;
import abs.sf.client.genie.exception.StringflowException;
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
public class GenieSDKInitializer extends JavaSDKInitializer {
	private static final Logger LOGGER = Logger.getLogger(GenieSDKInitializer.class.getName());

	private boolean isAlreadyLoggedIn;
	private String h2DbFilePath;

	public GenieSDKInitializer(String xmppServerHost, int xmppServerPort, boolean isAlreadyLoggedIn,
			String h2DbFilePath) {
		this(xmppServerHost, xmppServerPort, null, 0, isAlreadyLoggedIn, h2DbFilePath);
	}

	public GenieSDKInitializer(String xmppServerHost, int xmppServerPort, String sfcmSereverHost, int sfcmServerPort,
			boolean isAlreadyLoggedIn, String h2DbFilePath) {
		super(xmppServerHost, xmppServerPort, sfcmSereverHost, sfcmServerPort);
		this.isAlreadyLoggedIn = isAlreadyLoggedIn;
		this.h2DbFilePath = h2DbFilePath;
	}

	@Override
	public void init() throws InitializationErrorException {
		ConnectionManager.instantiate(this.xmppHost, this.xmppPort, this.sfcmHost, this.sfcmPort);

		XMPPConnection conn = ConnectionManager.getInstance().getXmppConnection();
		this.streamManager = new XMPPStreamManager(conn);

		this.correlator = new ResponseCorrelator();

		this.userManager = new AppUserManager(streamManager, correlator);

		try {

			this.chatManager = new AppChatManager(streamManager);

		} catch (StringflowException e) {
			LOGGER.log(Level.SEVERE, "Failed to create ChatManager", e);
			throw new InitializationErrorException(e);
		}

		this.presenceManager = new AppPresenceManager(streamManager);
		this.undeliveredStanzaManager = new UndeliveredStanzaManager(new UndeliverStanzaPersistenceMechanism());

		addEventListeners();

		if (!this.isAlreadyLoggedIn) {
			try {

				this.createDatabaseSchema(this.h2DbFilePath);

			} catch (StringflowException e) {
				LOGGER.log(Level.SEVERE, "Failed to create Db Schema", e);
				throw new InitializationErrorException(e);
			}
		}
	}

	/**
	 * Add core event handlers to {@link abs.ixi.client.core.event.EventBus}.
	 * Applications can further add/remove event handlers and listeners from
	 * event bus using {@link Platform}.
	 */
	private void addEventListeners() {
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
		Platform.addEventHandler(Event.EventType.MESSAGE_DELIVERED,
				((AppChatManager) this.chatManager).new MessageAckHandler());
	}

	private void createDatabaseSchema(final String h2DbFilePath) throws StringflowException {
		LOGGER.log(Level.INFO, "Initiating db schema using db file path : " + h2DbFilePath);

		try {
			SFSDKProperties.getInstance().setH2DbFilePath(h2DbFilePath);
			DbManager.getInstance().createDatabaseSchema();

		} catch (StringflowException e) {
			LOGGER.log(Level.WARNING, "Failed to create db schema", e);
			throw e;

		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to create db schema", e);
			throw new StringflowException(e.getMessage(), e);
		}

	}

}
