package abs.sf.client.gini.db;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import abs.ixi.client.core.InitializationErrorException;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.Stanza;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.h2.H2Database;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.messaging.Conversation;
import abs.sf.client.gini.messaging.MediaContent;
import abs.sf.client.gini.messaging.UserPresence;

/**
 * Manages database operations. Encapsulates database instance and it's one
 * point stop for all the database operations.
 */
public final class DbManager {
	private Database database;

	private static DbManager instance;

	private DbManager() throws DbException {
		this.database = new H2Database();
		try {

			this.database.init();

		} catch (InitializationErrorException e) {
			throw new DbException("Faied to initilize  database");
		}
	}

	public synchronized static DbManager getInstance() throws DbException {
		if (instance == null) {
			instance = new DbManager();
		}

		return instance;
	}

	public void createDatabaseSchema() throws DbException {
		this.database.createDatabaseSchema();
	}

	/**
	 * Closes database .
	 * 
	 * @throws DbException
	 */
	public void close() throws DbException {
		this.database.close();
	}

	/**
	 * It will clean up all data and drop all db objects.
	 * 
	 * @throws DbException
	 */
	public void dropDatabase() throws DbException {
		this.database.dropDatabase();
	}

	/**
	 * It will clean up all application data and but db objects will remain in
	 * db.
	 * 
	 * @throws DbException
	 */
	public void cleanUpAllData() throws DbException {
		this.database.cleanUpAllData();
	}

	/**
	 * Fetch all the active conversations. The method join on
	 * {@link RosterTable} to extract peer name (user/group). All the other
	 * required details are directly taken from {@link ConversationTable}.
	 * 
	 * @throws DbException
	 */
	public List<Conversation> fetchConversations() throws DbException {
		List<Conversation> conversations = this.database.fetchConversations();
		populatePresence(conversations);
		return conversations;
	}

	private void populatePresence(List<Conversation> conversations) throws DbException {
		for (Conversation cnv : conversations) {
			boolean online = isOnline(cnv.getPeerJid());
			cnv.setOnline(online);
		}
	}

	/**
	 * Check for user online status
	 * 
	 * @param userJID
	 * @return
	 * @throws DbException
	 */
	public boolean isOnline(String userJID) throws DbException {
		return this.database.isOnline(userJID);
	}

	/**
	 * Add a conversation into {@link ConversationTable}. The method does not
	 * check if there is already an entry against a JID. Therefore the caller
	 * must ensure that there is no pre-existing entry in the table; as there
	 * can be only one running conversation with a given JID.
	 *
	 * @param conv {@link Conversation} to be added into table
	 */
	public void addConversation(Conversation conv) throws DbException {
		this.database.addConversation(conv);
	}

	/**
	 * Update conversation
	 *
	 * @param conv
	 * @throws DbException
	 */
	public void updateConversation(Conversation conv) throws DbException {
		this.database.updateConversation(conv);
	}

	/**
	 * Update values for an existing Conversation in {@link ConversationTable}.
	 * The method does not check if the conversation exist in the table;
	 * therefore Caller must ensure that there such entry exist in the table.
	 *
	 * @param line
	 * @throws DbException
	 */
	public void addOrUpdateConversation(ChatLine line) throws DbException {
		if (this.conversationExists(line.getPeerBareJid())) {
			Conversation conv = new Conversation(line);

			if (line.getDirection() == ChatLine.Direction.RECEIVE) {
				int unread = this.getUnreadConversationCount(line.getPeerBareJid());
				conv.setUnreadChatLines(unread + 1);
			}

			this.updateConversation(conv);

		} else {
			Conversation conv = new Conversation(line);

			if (line.getDirection() == ChatLine.Direction.RECEIVE) {
				conv.setUnreadChatLines(1);
			}

			this.addConversation(conv);
		}
	}

	/**
	 * Get UnRead Conversation count from peerJID
	 * 
	 * @param peerJID
	 * @return
	 * @throws DbException
	 */
	public int getUnreadConversationCount(String peerJID) throws DbException {
		return this.database.getUnreadConversationCount(peerJID);
	}

	/**
	 * updating unread conversation count for peerJID with given count
	 * 
	 * @param peerJID
	 * @param unreadConversationCount
	 * @throws DbException
	 */
	public void updateUnreadConversationCount(String peerJID, int unreadConversationCount) throws DbException {
		this.database.updateUnreadConversationCount(peerJID, unreadConversationCount);
	}

	/**
	 * Check if there an active conversation with a given {@link JID}. If there
	 * is an entry found inside {@link ConversationTable}, it is assumed that
	 * there exist an active conversation with the given JID
	 *
	 * @param peerJid
	 * @return
	 * @throws DbException
	 */
	public boolean conversationExists(String peerJID) throws DbException {
		return this.database.conversationExists(peerJID);
	}

	/**
	 * Add a row in {@link ChatStoreTable}.
	 *
	 * @param line
	 * @return
	 * @throws DbException
	 */
	public void addToChatStore(ChatLine line) throws DbException {
		this.database.addToChatStore(line);
	}

	/**
	 * Update delivery status for a {@link ChatLine} in {@link ChatStoreTable}
	 *
	 * @param messageId
	 * @param messageStatus
	 * @throws SQLException
	 */
	public void updateDeliveryStatus(String messageId, ChatLine.MessageStatus messageStatus) throws DbException {
		this.database.updateDeliveryStatus(messageId, messageStatus);
	}

	/**
	 * Check message is delivered or not
	 * 
	 * @param messageId
	 * @return
	 * @throws DbException
	 */
	public boolean isMessageAlreadyDelivered(String messageId) throws DbException {
		return this.database.isMessageAlreadyDelivered(messageId);
	}

	public void markAsReceived(String messageId) throws DbException {
		this.updateDeliveryStatus(messageId, ChatLine.MessageStatus.DELIVERED_TO_RECEIVER);
	}

	public void markAsAcknowledged(String messageId) throws DbException {
		this.updateDeliveryStatus(messageId, ChatLine.MessageStatus.RECEIVER_IS_ACKNOWLEDGED);
	}

	public void markAsDisplayed(String messageId, String pearJID) throws DbException {
		this.updateDeliveryStatus(messageId, ChatLine.MessageStatus.RECEIVER_HAS_VIEWED);
	}

	/**
	 * Update marking status for a {@link ChatLine} in {@link ChatStoreTable}
	 *
	 * @param messageId
	 * @throws SQLException
	 */
	public void markMessageViewed(String messageId) throws DbException {
		this.database.markMessageViewed(messageId);
	}

	/**
	 * Fetch All unRead chatLines for a given pear {@link JID}
	 *
	 * @param pearJID
	 * @return
	 */
	public List<ChatLine> getAllUnreadChatLines(String pearJID) throws DbException {
		return this.database.getAllUnreadChatLines(pearJID);
	}

	/**
	 * Fetch all the {@link ChatLine} stored in {@link ChatStoreTable} for a
	 * conversation based on peer {@link JID}
	 *
	 * @param pearJID {@link JID }of the peer
	 * @return List of chatLine objects
	 */
	public List<ChatLine> fetchConversationChatlines(String pearJID) throws DbException {
		return this.database.fetchConversationChatlines(pearJID);
	}

	/**
	 * Check message with given messageId for that pearJID already exist or not.
	 * 
	 * @param peerJID
	 * @param messageId
	 * @return
	 * @throws DbException
	 */
	public boolean isMessageAlreadyExist(String pearJID, String messageId) throws DbException {
		return this.database.isMessageAlreadyExist(pearJID, messageId);
	}

	/**
	 * Fetch all the chatlines which have not been delivered to server as yet. A
	 * {@link ChatLine} with delivery status 0 indicates undelivered chatline.
	 *
	 * @return
	 */
	public List<ChatLine> getUndeliveredMessages() throws DbException {
		return this.database.getUndeliveredMessages();
	}

	/**
	 * Add a roster item into {@link RosterTable}. The method does not check if
	 * there is already an entry for the same JID. Therefore caller must ensure
	 * that it's adding an item which does not exist in the table already
	 *
	 * @param item
	 */
	public void addRosterItem(RosterItem item) throws DbException {
		this.database.addRosterItem(item);
	}

	/**
	 * Update an existing {@link RosterItem}. The method does not check if the
	 * item already exist in the table. Therefore caller must ensure that item
	 * is present in the table.
	 *
	 * @param item
	 */
	public void updateRosterItem(RosterItem item) throws DbException {
		this.database.updateRosterItem(item);
	}

	/**
	 * Fetching all Roster items
	 * 
	 * @return
	 * @throws DbException
	 */
	public List<RosterItem> getRosterList() throws DbException {
		return this.database.getRosterList();
	}

	/**
	 * Deleting Roster item
	 * 
	 * @param item
	 * @throws DbException
	 */
	public void deleteRosterItem(RosterItem item) throws DbException {
		this.deleteRosterItem(item.getJid().getBareJID());
	}

	/**
	 * Deleting Roster item
	 * 
	 * @param jid
	 * @throws DbException
	 */
	public void deleteRosterItem(String jid) throws DbException {
		this.database.deleteRosterItem(jid);
	}

	/**
	 * Getting roster item name
	 * 
	 * @param itemJID
	 * @return
	 * @throws DbException
	 */
	public String getRosterItemName(String itemJID) throws DbException {
		return this.database.getRosterItemName(itemJID);
	}

	/**
	 * updating roster items
	 * 
	 * @param list
	 * @throws DbException
	 */
	public void addOrUpdateRoster(List<RosterItem> list) throws DbException {
		for (RosterItem item : list) {
			addOrUpdateRosterItem(item);
		}
	}

	/**
	 * updating Roster item
	 * 
	 * @param item
	 * @throws DbException
	 */
	public void addOrUpdateRosterItem(RosterItem item) throws DbException {
		this.database.addOrUpdateRosterItem(item);
	}

	/**
	 * Clear Roster data
	 */
	public void clearRosterData() throws DbException {
		this.database.clearRosterData();
	}

	/**
	 * Check is given {@link JID} item is group.
	 * 
	 * @param jid
	 * @return
	 * @throws DbException
	 */
	public boolean isRosterGroup(String jid) throws DbException {
		return this.database.isRosterGroup(jid);
	}

	/**
	 * Updating ChatRooms
	 * 
	 * @param chatRoom
	 * @throws DbException
	 */
	public void addOrUpdateChatRoom(ChatRoom chatRoom) throws DbException {
		this.database.addOrUpdateChatRoom(chatRoom);
	}

	/**
	 * Adding new chat room
	 * 
	 * @param chatRoom
	 * @throws DbException
	 */
	public void addChatRoom(ChatRoom chatRoom) throws DbException {
		this.database.addChatRoom(chatRoom);
	}

	/**
	 * Updating Chat room data
	 * 
	 * @param chatRoom
	 * @throws DbException
	 */
	public void updateChatRoom(ChatRoom chatRoom) throws DbException {
		this.database.updateChatRoom(chatRoom);
	}

	/**
	 * Updating ChatRoom Subject
	 * 
	 * @param roomJID
	 * @param subject
	 * @throws DbException
	 */
	public void updateChatRoomSubject(String roomJID, String subject) throws DbException {
		this.database.updateChatRoomSubject(roomJID, subject);
	}

	public String getChatRoomSubject(String roomJID) throws DbException {
		return this.database.getChatRoomSubject(roomJID);
	}

	public String getChatRoomMemberJID(String roomJID, String memberNickName) throws DbException {
		return this.database.getChatRoomMemberJID(roomJID, memberNickName);
	}

	public void deleteChatRoom(ChatRoom chatRoom) throws DbException {
		deleteChatRoom(chatRoom.getRoomJID().getBareJID());
	}

	public void deleteChatRoom(String roomJID) throws DbException {
		deleteRosterItem(roomJID);
		deleteAllRoomMembers(roomJID);
	}

	public void addChatRoomMembers(ChatRoom chatRoom) throws DbException {
		Set<ChatRoom.ChatRoomMember> chatRoomMembers = chatRoom.getMembers();

		for (ChatRoom.ChatRoomMember member : chatRoomMembers) {
			addChatRoomMember(member);
		}
	}

	public boolean isChatRoomMember(JID roomJID, JID memberJID) throws DbException {
		return this.database.isChatRoomMember(roomJID, memberJID);
	}

	public void addOrUpdateChatRoomMember(ChatRoom.ChatRoomMember member) throws DbException {
		this.database.addOrUpdateChatRoomMember(member);
	}

	public void addChatRoomMember(ChatRoom.ChatRoomMember member) throws DbException {
		this.database.addChatRoomMember(member);

	}

	public void updateChatRoomMember(ChatRoom.ChatRoomMember member) throws DbException {
		this.database.updateChatRoomMember(member);
	}

	public void deleteAllRoomMembers(String roomJID) throws DbException {
		this.database.deleteAllRoomMembers(roomJID);
	}

	public void removeRoomMember(ChatRoom.ChatRoomMember member) throws DbException {
		removeRoomMember(member.getRoomJID().getBareJID(), member.getUserJID().getBareJID());
	}

	public void removeRoomMember(String roomJID, String memberJID) throws DbException {
		this.database.removeRoomMember(roomJID, memberJID);
	}

	public String getChatRoomMemberName(String roomJId, String memberJID) throws DbException {
		String memberName = getRosterItemName(memberJID);

		if (StringUtils.isNullOrEmpty(memberName)) {
			memberName = getRoomMemberNickName(roomJId, memberJID);

			if (StringUtils.isNullOrEmpty(memberName)) {
				try {
					memberName = new JID(memberJID).getNode();
				} catch (InvalidJabberId e) {
					memberName = "unknown";
				}
			}

		}

		return memberName;
	}

	public String getRoomMemberNickName(String roomJId, String memberJID) throws DbException {
		return this.database.getRoomMemberNickName(roomJId, memberJID);
	}

	public ChatRoom getChatRoomDetails(String roomJID) throws DbException {
		return this.database.getChatRoomDetails(roomJID);

	}

	public JID getChatRoomJID(String roomName) throws DbException {
		return this.database.getChatRoomJID(roomName);
	}

	public List<ChatRoom> getChatRooms() throws DbException {
		return this.database.getChatRooms();
	}

	public Set<ChatRoom.ChatRoomMember> getChatRoomMembers(ChatRoom room) throws DbException {
		return this.database.getChatRoomMembers(room);
	}

	public void addPresence(String userJID, PresenceType presence, String mood, String status) throws DbException {
		this.database.addPresence(userJID, presence, mood, status);
	}

	public void updatePresence(String userJID, PresenceType presence, String mood, String status) throws DbException {
		this.database.updatePresence(userJID, presence, mood, status);
	}

	public void updatePresence(String userJID, PresenceType presence) throws DbException {
		this.database.updatePresence(userJID, presence, null, null);
	}

	public void addOrUpdatePresence(String userJID, PresenceType presence, String mood, String status)
			throws DbException {
		this.database.addOrUpdatePresence(userJID, presence, mood, status);
	}

	public void addOrUpdatePresence(String userJID, PresenceType presence) throws DbException {
		this.database.addOrUpdatePresence(userJID, presence, null, null);
	}

	public UserPresence getPresenceDetails(String userJID) throws DbException {
		return this.database.getPresenceDetails(userJID);
	}

	public void deleteUserPresence(String userJID) throws DbException {
		this.database.deleteUserPresence(userJID);
	}

	public void addOrUpdateUserProfileData(UserProfileData userProfileData) throws DbException {
		this.database.addOrUpdateUserProfileData(userProfileData);
	}

	public void addUserProfile(UserProfileData userProfileData) throws DbException {
		this.database.addUserProfile(userProfileData);
	}

	public void updateUserProfile(UserProfileData userProfileData) throws DbException {
		this.database.updateUserProfile(userProfileData);
	}

	public UserProfileData getUserProfileData(String userJID) throws DbException {
		return this.database.getUserProfileData(userJID);
	}

	public InputStream getUserAvatar(String userJID) throws DbException {
		return this.database.getUserAvatarBytes(userJID);
	}

	public long storeMedia(String mediaId, byte[] mediaThumb, String mediaPath, String contentType) throws DbException {
		return this.database.storeMedia(mediaId, mediaThumb, mediaPath, contentType);

	}

	public void updateMediaPath(String mediaId, String mediaPath) throws DbException {
		this.database.updateMediaPath(mediaId, mediaPath);
	}

	public MediaContent getMediaDetaisByMediaId(String mediaId) throws DbException {
		return this.database.getMediaDetaisByMediaId(mediaId);

	}

	public MediaContent getMediaDetaisByMediaUUID(Long uuid) throws DbException {
		return this.database.getMediaDetaisByMediaUUID(uuid);
	}

	public String getMediaPathByMediaId(String mediaId) throws DbException {
		return this.database.getMediaPathByMediaId(mediaId);
	}

	public String getMediaPathByMediaUUID(Long uuid) throws DbException {
		return this.database.getMediaPathByMediaUUID(uuid);
	}

	public void deleteMedia(String mediaId) throws DbException {
		this.database.deleteMedia(mediaId);
	}

	public void deleteMedia(Long mediaId) throws DbException {
		this.database.deleteMedia(mediaId);
	}

	public void deleteFirstUndeliveredStanza(int stanzaCount) throws DbException {
		this.database.deleteFirstUndeliveredStanza(stanzaCount);
	}

	public void persistUndeliverStanza(Stanza stanza) throws DbException {
		this.database.persistUndeliverStanza(stanza);
	}

	public void deleteAllUndeliverStanzas() throws DbException {
		this.database.deleteAllUndeliverStanzas();
	}

	public List<Stanza> fetchAllUndeliverStanzas() throws DbException {
		List<Stanza> stanzas = this.database.fetchAllUndeliverStanzas();

		if (!CollectionUtils.isNullOrEmpty(stanzas)) {
			this.deleteAllUndeliverStanzas();
		}

		return stanzas;
	}

}
