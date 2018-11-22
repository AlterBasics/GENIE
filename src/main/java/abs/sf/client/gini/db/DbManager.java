package abs.sf.client.gini.db;

import java.sql.SQLException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import abs.ixi.client.core.Platform;
import abs.ixi.client.core.Session;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.ObjectUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Presence.PresenceType;
import abs.ixi.client.xmpp.packet.Roster.RosterItem;
import abs.ixi.client.xmpp.packet.Stanza;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.h2.SQLQuery;
import abs.sf.client.gini.db.mapper.ChatRoomMemberRowMapper;
import abs.sf.client.gini.db.mapper.ChatRoomRowMapper;
import abs.sf.client.gini.db.mapper.MediaRowMapper;
import abs.sf.client.gini.db.mapper.PresenceRowMapper;
import abs.sf.client.gini.db.mapper.RowMapper;
import abs.sf.client.gini.db.mapper.UndeliverStanzaRowMapper;
import abs.sf.client.gini.db.mapper.UserProfileRowMapper;
import abs.sf.client.gini.db.object.ChatRoomMemberTable;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.MediaStoreTable;
import abs.sf.client.gini.db.object.PresenceTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.db.object.UndeliverStanzaTable;
import abs.sf.client.gini.db.object.UserProfileTable;
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

	private DbManager() {

	}

	public synchronized static DbManager getInstance() {
		if (instance == null) {
			instance = new DbManager();
		}

		return instance;
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
	public List<ChatLine> fetchConversationChatlines(String pearJID, boolean isGroup) throws DbException {
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

	public String getChatRoomSubject(String roomJID) {
		return dbHelper.queryString(SQLQuery.FETCH_CHAT_ROOM_SUBJECT, new String[] { roomJID });
	}

	public String getChatRoomMemberJID(String roomJID, String memberNickName) {
		return dbHelper.queryString(SQLQuery.FETCH_ROOM_MEMBER_JID, new String[] { roomJID, memberNickName });
	}

	public void deleteChatRoom(ChatRoom chatRoom) {
		deleteChatRoom(chatRoom.getRoomJID().getBareJID());
	}

	public void deleteChatRoom(String roomJID) {
		deleteRosterItem(roomJID);
		deleteAllRoomMembers(roomJID);
	}

	public void addChatRoomMembers(ChatRoom chatRoom) {
		Set<ChatRoom.ChatRoomMember> chatRoomMembers = chatRoom.getMembers();

		for (ChatRoom.ChatRoomMember member : chatRoomMembers) {
			addChatRoomMember(member);
		}
	}

	public boolean isChatRoomMember(JID roomJID, JID memberJID) {
		long count = dbHelper.queryInt(SQLQuery.FETCH_CHAT_ROOM_MEMBER_COUNT,
				new String[] { memberJID.getBareJID(), roomJID.getBareJID() });

		return count == 1;
	}

	public void addOrUpdateChatRoomMember(ChatRoom.ChatRoomMember member) {
		synchronized (ChatRoomMemberTable.class) {
			long count = dbHelper.queryInt(SQLQuery.FETCH_CHAT_ROOM_MEMBER_COUNT,
					new String[] { member.getUserJID().getBareJID(), member.getRoomJID().getBareJID() });

			if (count == 0) {
				addChatRoomMember(member);

			} else {
				updateChatRoomMember(member);
			}
		}
	}

	public void addChatRoomMember(ChatRoom.ChatRoomMember member) {
		ContentValues memberContent = new ContentValues();
		memberContent.put(ChatRoomMemberTable.COLUMN_MEMBER_JID, member.getUserJID().getBareJID());
		memberContent.put(ChatRoomMemberTable.COLUMN_MEMBER_NICK_NAME, member.getNickName());
		memberContent.put(ChatRoomMemberTable.COLUMN_AFFILATION, member.getAffiliation().val());
		memberContent.put(ChatRoomMemberTable.COLUMN_ROLE, member.getRole().val());
		memberContent.put(ChatRoomMemberTable.COLUMN_ROOM_JID, member.getRoomJID().getBareJID());

		this.dbHelper.insert(ChatRoomMemberTable.TABLE_NAME, memberContent);
	}

	public void updateChatRoomMember(ChatRoom.ChatRoomMember member) {
		ContentValues memberContent = new ContentValues();

		if (member.getNickName() != null)
			memberContent.put(ChatRoomMemberTable.COLUMN_MEMBER_NICK_NAME, member.getNickName());

		if (member.getAffiliation() != null)
			memberContent.put(ChatRoomMemberTable.COLUMN_AFFILATION, member.getAffiliation().val());

		if (member.getRole() != null)
			memberContent.put(ChatRoomMemberTable.COLUMN_ROLE, member.getRole().val());

		this.dbHelper.update(ChatRoomMemberTable.TABLE_NAME, memberContent,
				ChatRoomMemberTable.COLUMN_MEMBER_JID + " = ? AND " + ChatRoomMemberTable.COLUMN_ROOM_JID + " = ? ",
				new String[] { member.getUserJID().getBareJID(), member.getRoomJID().getBareJID() });
	}

	public void deleteAllRoomMembers(String roomJID) {
		dbHelper.delete(ChatRoomMemberTable.TABLE_NAME, ChatRoomMemberTable.COLUMN_ROOM_JID, roomJID);
	}

	public void removeRoomMember(ChatRoom.ChatRoomMember member) {
		removeRoomMember(member.getRoomJID().getBareJID(), member.getUserJID().getBareJID());
	}

	public void removeRoomMember(String roomJID, String memberJID) {
		dbHelper.query(SQLQuery.DELETE_ROOM_MEMBER, new String[] { memberJID, roomJID }, null);
	}

	public String getChatRoomMemberName(String roomJId, String memberJID) {
		String memberName = getRosterItemName(memberJID);

		if (StringUtils.isNullOrEmpty(memberName)) {
			memberName = getRoomMemberNickName(roomJId, memberJID);

			if (StringUtils.isNullOrEmpty(memberName)) {
				try {
					memberName = new JID(memberJID).getNode();
				} catch (InvalidJabberId e) {
					memberName = "unknown";
					// Swallow it
				}
			}

		}

		return memberName;
	}

	private String getRoomMemberNickName(String roomJId, String memberJID) {
		return dbHelper.queryString(SQLQuery.FETCH_ROOM_MEMBER_NICK_NAME, new String[] { memberJID, roomJId });
	}

	public ChatRoom getChatRoomDetails(String roomJID) {
		ChatRoom room = dbHelper.queryForObject(SQLQuery.FETCH_CHAT_ROOM_DETAILS, new String[] { roomJID },
				new ChatRoomRowMapper());

		if (room != null) {
			room.setMembers(getChatRoomMembers(room));
		}

		return room;
	}

	public JID getChatRoomJID(String roomName) {
		String roomJID = dbHelper.queryString(SQLQuery.FETCH_CHAT_ROOM_JID, new String[] { roomName });

		if (!StringUtils.isNullOrEmpty(roomJID)) {
			try {
				return new JID(roomJID);

			} catch (Exception e) {
				// Swallow Exception
			}
		}

		return null;
	}

	public List<ChatRoom> getChatRooms() {
		List<ChatRoom> rooms = dbHelper.query(SQLQuery.FETCH_CHAT_ROOMS, new ChatRoomRowMapper());

		for (ChatRoom room : rooms) {
			room.setMembers(getChatRoomMembers(room));
		}

		return rooms;
	}

	public Set<ChatRoom.ChatRoomMember> getChatRoomMembers(ChatRoom room) {
		List<ChatRoom.ChatRoomMember> members = dbHelper.query(SQLQuery.FETCH_CHAT_ROOM_MEMBERS,
				new String[] { room.getRoomJID().getBareJID() }, new ChatRoomMemberRowMapper(room));

		return new HashSet<>(members);
	}

	public void addPresence(String userJID, PresenceType presence, String mood, String status) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(PresenceTable.COLUMN_JID, userJID);
		contentValues.put(PresenceTable.COLUMN_PRESNCE, presence.val());
		contentValues.put(PresenceTable.COLUMN_MOOD, mood);
		contentValues.put(PresenceTable.COLUMN_STATUS, status);
		contentValues.put(PresenceTable.COLUMN_LAST_UPDATE, DateUtils.currentTimeInMiles());

		dbHelper.insert(PresenceTable.TABLE_NAME, contentValues);
	}

	public void updatePresence(String userJID, PresenceType presence, String mood, String status) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(PresenceTable.COLUMN_JID, userJID);
		contentValues.put(PresenceTable.COLUMN_PRESNCE, presence.val());
		contentValues.put(PresenceTable.COLUMN_MOOD, mood);
		contentValues.put(PresenceTable.COLUMN_STATUS, status);
		contentValues.put(PresenceTable.COLUMN_LAST_UPDATE, DateUtils.currentTimeInMiles());

		dbHelper.update(PresenceTable.TABLE_NAME, contentValues, PresenceTable.COLUMN_JID + " = ?",
				new String[] { userJID });
	}

	public void updatePresence(String userJID, PresenceType presence) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(PresenceTable.COLUMN_JID, userJID);
		contentValues.put(PresenceTable.COLUMN_PRESNCE, presence.val());
		contentValues.put(PresenceTable.COLUMN_LAST_UPDATE, DateUtils.currentTimeInMiles());

		dbHelper.update(PresenceTable.TABLE_NAME, contentValues, PresenceTable.COLUMN_JID + " = ?",
				new String[] { userJID });
	}

	public void addOrUpdatePresence(String userJID, PresenceType presence, String mood, String status) {
		synchronized (PresenceTable.class) {
			long count = dbHelper.queryInt(SQLQuery.FETCH_PRESENCE_COUNT, new String[] { userJID });

			if (count == 0) {
				addPresence(userJID, presence, mood, status);

			} else {
				updatePresence(userJID, presence, mood, status);
			}
		}
	}

	public void addOrUpdatePresence(String userJID, PresenceType presence) throws SQLException {
		synchronized (PresenceTable.class) {
			long count = dbHelper.queryInt(SQLQuery.FETCH_PRESENCE_COUNT, new String[] { userJID });

			if (count == 0) {
				addPresence(userJID, presence, null, null);

			} else {
				updatePresence(userJID, presence);
			}
		}
	}

	public UserPresence getPresenceDetails(String userJID) {
		return dbHelper.queryForObject(SQLQuery.FETCH_PRESENCE_DETAILS, new String[] { userJID },
				new PresenceRowMapper());
	}

	public void deleteUserPresence(String userJID) {
		dbHelper.delete(PresenceTable.TABLE_NAME, PresenceTable.COLUMN_JID, userJID);
	}

	public void addOrUpdateUserProfileData(UserProfileData userProfileData) {
		synchronized (UserProfileTable.class) {
			long count = dbHelper.queryInt(SQLQuery.FETCH_USER_PROFILE_COUNT,
					new String[] { userProfileData.getJabberId().getBareJID() });

			if (count == 0) {
				addUserProfile(userProfileData);

			} else {
				updateUserProfile(userProfileData);
			}
		}
	}

	private long addUserProfile(UserProfileData userProfileData) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(UserProfileTable.COLUMN_JID, userProfileData.getJabberId().getBareJID());
		contentValues.put(UserProfileTable.COLUMN_FIRST_NAME, userProfileData.getFirstName());
		contentValues.put(UserProfileTable.COLUMN_MIDDLE_NAME, userProfileData.getMiddleName());
		contentValues.put(UserProfileTable.COLUMN_LAST_NAME, userProfileData.getLastName());
		contentValues.put(UserProfileTable.COLUMN_NICK_NAME, userProfileData.getNickName());
		contentValues.put(UserProfileTable.COLUMN_EMAIL, userProfileData.getEmail());
		contentValues.put(UserProfileTable.COLUMN_PHONE, userProfileData.getPhone());
		contentValues.put(UserProfileTable.COLUMN_GENDER, userProfileData.getGender());
		contentValues.put(UserProfileTable.COLUMN_BDAY, userProfileData.getBday());

		if (userProfileData.getAddress() != null) {
			UserProfileData.Address address = userProfileData.getAddress();

			contentValues.put(UserProfileTable.COLUMN_ADDRESS_HOME, address.getHome());
			contentValues.put(UserProfileTable.COLUMN_ADDRESS_STREET, address.getStreet());
			contentValues.put(UserProfileTable.COLUMN_ADDRESS_LOCALITY, address.getLocality());
			contentValues.put(UserProfileTable.COLUMN_ADDRESS_CITY, address.getCity());
			contentValues.put(UserProfileTable.COLUMN_ADDRESS_STATE, address.getState());
			contentValues.put(UserProfileTable.COLUMN_ADDRESS_COUNTRY, address.getCountry());
			contentValues.put(UserProfileTable.COLUMN_ADDRESS_PCODE, address.getPcode());
		}

		if (userProfileData.getAvtar() != null) {
			UserProfileData.UserAvtar avtar = userProfileData.getAvtar();

			if (StringUtils.isNullOrEmpty(avtar.getBase64EncodedImage())) {
				contentValues.put(UserProfileTable.COLUMN_AVATAR, new byte[0]);

			} else {
				contentValues.put(UserProfileTable.COLUMN_AVATAR,
						Base64.decode(avtar.getBase64EncodedImage(), Base64.DEFAULT));
			}

			contentValues.put(UserProfileTable.COLUMN_AVATAR_MEDIA_TYPE, avtar.getImageType());
		}

		if (userProfileData.getDescription() != null) {
			contentValues.put(UserProfileTable.COLUMN_ABOUT, userProfileData.getDescription());
		}

		return dbHelper.insert(UserProfileTable.TABLE_NAME, contentValues);
	}

	public long updateUserProfile(UserProfileData userProfileData) {
		ContentValues contentValues = new ContentValues();

		if (userProfileData.getFirstName() != null)
			contentValues.put(UserProfileTable.COLUMN_FIRST_NAME, userProfileData.getFirstName());

		if (userProfileData.getMiddleName() != null)
			contentValues.put(UserProfileTable.COLUMN_MIDDLE_NAME, userProfileData.getMiddleName());

		if (userProfileData.getLastName() != null)
			contentValues.put(UserProfileTable.COLUMN_LAST_NAME, userProfileData.getLastName());

		if (userProfileData.getNickName() != null)
			contentValues.put(UserProfileTable.COLUMN_NICK_NAME, userProfileData.getNickName());

		if (userProfileData.getEmail() != null)
			contentValues.put(UserProfileTable.COLUMN_EMAIL, userProfileData.getEmail());

		if (userProfileData.getPhone() != null)
			contentValues.put(UserProfileTable.COLUMN_PHONE, userProfileData.getPhone());

		if (userProfileData.getGender() != null)
			contentValues.put(UserProfileTable.COLUMN_GENDER, userProfileData.getGender());

		if (userProfileData.getBday() != null)
			contentValues.put(UserProfileTable.COLUMN_BDAY, userProfileData.getBday());

		if (userProfileData.getAddress() != null) {
			UserProfileData.Address address = userProfileData.getAddress();

			if (address.getHome() != null)
				contentValues.put(UserProfileTable.COLUMN_ADDRESS_HOME, address.getHome());

			if (address.getStreet() != null)
				contentValues.put(UserProfileTable.COLUMN_ADDRESS_STREET, address.getStreet());

			if (address.getLocality() != null)
				contentValues.put(UserProfileTable.COLUMN_ADDRESS_LOCALITY, address.getLocality());

			if (address.getCity() != null)
				contentValues.put(UserProfileTable.COLUMN_ADDRESS_CITY, address.getCity());

			if (address.getState() != null)
				contentValues.put(UserProfileTable.COLUMN_ADDRESS_STATE, address.getState());

			if (address.getCountry() != null)
				contentValues.put(UserProfileTable.COLUMN_ADDRESS_COUNTRY, address.getCountry());

			if (address.getPcode() != null)
				contentValues.put(UserProfileTable.COLUMN_ADDRESS_PCODE, address.getPcode());
		}

		if (userProfileData.getAvtar() != null) {
			UserProfileData.UserAvtar avtar = userProfileData.getAvtar();

			if (StringUtils.isNullOrEmpty(avtar.getBase64EncodedImage())) {
				contentValues.put(UserProfileTable.COLUMN_AVATAR, new byte[0]);

			} else {
				contentValues.put(UserProfileTable.COLUMN_AVATAR,
						Base64.decode(avtar.getBase64EncodedImage(), Base64.DEFAULT));
			}

			contentValues.put(UserProfileTable.COLUMN_AVATAR_MEDIA_TYPE, avtar.getImageType());
		}

		if (userProfileData.getDescription() != null) {
			contentValues.put(UserProfileTable.COLUMN_ABOUT, userProfileData.getDescription());
		}

		return dbHelper.update(UserProfileTable.TABLE_NAME, contentValues, UserProfileTable.COLUMN_JID + " = ?",
				new String[] { userProfileData.getJabberId().getBareJID() });

	}

	public UserProfileData getUserProfileData(String userJID) {
		UserProfileData userProfileData = dbHelper.queryForObject(SQLQuery.FETCH_USER_PROFILE_DATA,
				new String[] { userJID }, new UserProfileRowMapper());
		return userProfileData;
	}

	public byte[] getUserAvatarBytes(String userJID) {
		return dbHelper.queryForObject(SQLQuery.FETCH_USER_PROFILE_AVATAR, new String[] { userJID },
				new RowMapper<byte[]>() {

					@Override
					public byte[] map(Cursor cursor) throws SQLException {
						return cursor.getBlob(0);
					}
				});
	}

	public long storeMedia(String mediaId, byte[] mediaThumb, String mediaPath, String contentType) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStoreTable.COL_MEDIA_ID, mediaId);
		contentValues.put(MediaStoreTable.COL_MEDIA_THUMB, mediaThumb);
		contentValues.put(MediaStoreTable.COL_MEDIA_PATH, mediaPath);
		contentValues.put(MediaStoreTable.COL_CONTENT_TYPE, contentType);

		return dbHelper.insert(MediaStoreTable.TABLE_NAME, contentValues);
	}

	public void updateMediaPath(String mediaId, String mediaPath) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStoreTable.COL_MEDIA_ID, mediaId);
		contentValues.put(MediaStoreTable.COL_MEDIA_PATH, mediaPath);

		dbHelper.update(MediaStoreTable.TABLE_NAME, contentValues, MediaStoreTable.COL_MEDIA_ID + " = ?",
				new String[] { mediaId });
	}

	public MediaContent getMediaDetaisByMediaId(String mediaId) {
		return dbHelper.queryForObject(SQLQuery.FETCH_MEDIA_DETAILS_BY_MEDIA_ID, new String[] { mediaId },
				new MediaRowMapper());
	}

	public MediaContent getMediaDetaisByMediaUUID(Long uuid) {
		return dbHelper.queryForObject(SQLQuery.FETCH_MEDIA_DETAILS_BY_UUID, new String[] { uuid.toString() },
				new MediaRowMapper());
	}

	public String getMediaPathByMediaId(String mediaId) {
		return dbHelper.queryString(SQLQuery.FETCH_MEDIA_PATH_BY_MEDIA_ID, new String[] { mediaId });
	}

	public String getMediaPathByMediaUUID(Long uuid) {
		return dbHelper.queryString(SQLQuery.FETCH_MEDIA_PATH_BY_UUID, new String[] { uuid.toString() });
	}

	public void deleteMedia(String mediaId) {
		dbHelper.delete(MediaStoreTable.TABLE_NAME, MediaStoreTable.COL_MEDIA_ID + " = ?", mediaId);
	}

	public void deleteMedia(Long mediaId) {
		dbHelper.delete(MediaStoreTable.TABLE_NAME, MediaStoreTable.COL_UUID + " = ?", mediaId.toString());
	}

	public void deleteFirstUndeliveredStanza(Integer stanzaCount) {
		dbHelper.query(SQLQuery.DELETE_FIRST_UNDELIVERED_STANZAS, new String[] { stanzaCount.toString() }, null);
	}

	public void persistUndeliverStanza(Stanza stanza) {
		ContentValues contentValues = new ContentValues();

		contentValues.put(UndeliverStanzaTable.COL_STANZA, ObjectUtils.serializeObject(stanza));

		dbHelper.insert(UndeliverStanzaTable.TABLE_NAME, contentValues);
	}

	public void deleteAllUndeliverStanzas() {
		this.truncateTable(UndeliverStanzaTable.TABLE_NAME);
	}

	public List<Stanza> fetchAllUndeliverStanzas() {
		synchronized (UndeliverStanzaTable.class) {
			List<Stanza> stanzas = dbHelper.query(SQLQuery.FETCH_ALL_UNDELIVERD_STANZAS,
					new UndeliverStanzaRowMapper());

			if (!CollectionUtils.isNullOrEmpty(stanzas)) {
				this.deleteAllUndeliverStanzas();
			}

			return stanzas;
		}
	}

}
