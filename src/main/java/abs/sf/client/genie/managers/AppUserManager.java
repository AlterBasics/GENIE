package abs.sf.client.genie.managers;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.DeviceType;
import abs.ixi.client.Platform;
import abs.ixi.client.PushNotificationService;
import abs.ixi.client.UserManager;
import abs.ixi.client.core.Callback;
import abs.ixi.client.core.ResponseCorrelator;
import abs.ixi.client.io.XMPPStreamManager;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.util.UUIDGenerator;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Roster;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.client.genie.db.DbManager;
import abs.sf.client.genie.db.exception.DbException;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.utils.GenieSDKInitializer;
import abs.sf.client.genie.utils.SFSDKProperties;

public class AppUserManager extends UserManager {
	private static final Logger LOGGER = Logger.getLogger(AppUserManager.class.getName());

	public AppUserManager(XMPPStreamManager streamManager, ResponseCorrelator responseCorrelator) {
		super(streamManager, responseCorrelator);
	}

	/**
	 * Check given {@link JID} is a chat room (group) {@link JID}.
	 *
	 * @param jid
	 * @return
	 * @throws StringflowErrorException
	 */
	public boolean checkIsChatRoom(JID jid) throws StringflowErrorException {
		try {
			return DbManager.getInstance().isRosterGroup(jid.getBareJID());

		} catch (DbException e) {
			String errorMessage = "Failed to check given jid " + jid.getBareJID()
					+ " of a group due to database operations failure";

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * Check that given contact is a member of a given group.
	 *
	 * @param userJID
	 * @param groupJID
	 * @return
	 * @throws StringflowErrorException
	 */
	public boolean checkIsChatRoomMember(JID userJID, JID groupJID) throws StringflowErrorException {
		try {
			return DbManager.getInstance().isChatRoomMember(groupJID, userJID);

		} catch (DbException e) {
			String errorMessage = "Failed to check given user with jid " + userJID.getBareJID() + " in a group : "
					+ groupJID.getBareJID() + " due to database operations failure";

			LOGGER.log(Level.WARNING, errorMessage, e);

			throw new StringflowErrorException(errorMessage + " due to database operations failure", e);
		}
	}

	/**
	 * @return All user contact members and groups list in the form of
	 *         {@link abs.ixi.client.xmpp.packet.Roster.RosterItem}'s list.
	 * @throws StringflowErrorException
	 */
	public List<Roster.RosterItem> getRosterItemList() throws StringflowErrorException {
		try {

			return DbManager.getInstance().getRosterList();

		} catch (DbException e) {
			String errorMessage = "Failed to get Roster Items list due to database operations failure";

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * @param roomJID
	 * @return all details of given {@link JID}'s group.
	 */
	public ChatRoom getChatRoomDetails(JID roomJID) throws StringflowErrorException {
		try {
			return DbManager.getInstance().getChatRoomDetails(roomJID.getBareJID());

		} catch (DbException e) {
			String errorMessage = "Failed to get chatRoom Details due to database operations failure";

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * ChatRoom subject from cache
	 * 
	 * @param contactJID
	 * @return room subject
	 * @throws StringflowErrorException
	 */
	public String getChatRoomSubject(JID roomJID) throws StringflowErrorException {
		try {
			return DbManager.getInstance().getChatRoomSubject(roomJID.getBareJID());

		} catch (DbException e) {
			String errorMessage = "Failed to get chatRoom subject due to database operations failure";

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * @param itemJID
	 * @return Actual name of given {@link JID}'s user.
	 * @throws StringflowErrorException
	 */
	public String getRosterItemName(JID itemJID) throws StringflowErrorException {
		try {
			return DbManager.getInstance().getRosterItemName(itemJID.getBareJID());

		} catch (DbException e) {
			String errorMessage = "Failed to get Roster Item name due to database operations failure";

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * Request to get chat rooms list.
	 */
	public void sendGetChatRoomListRequest() {
		this.sendGetChatRoomsRequest(new Callback<List<ChatRoom>, Exception>() {
			@Override
			public void onSuccess(List<ChatRoom> rooms) {
				if (!CollectionUtils.isNullOrEmpty(rooms)) {
					for (ChatRoom room : rooms) {
						sendGetChatRoomInfoRequest(room.getRoomJID());
						sendGetChatRoomMembersRequest(room.getRoomJID());
					}
				}
			}

			@Override
			public void onFailure(Exception arg) {
				// For now do nothing
			}
		});
	}

	/**
	 * Create private room with given room name. No one can enter in this room
	 * using {@link UserManager#sendJoinChatRoomRequest(JID, String)}. Only room
	 * admin and owner can add or remove other members.
	 *
	 * @param groupName
	 * @param members group members list
	 * @return
	 * @throws StringflowErrorException
	 */
	public boolean createPrivateGroup(String groupName, List<JID> members) throws StringflowErrorException {
		try {
			String chatRoomName = generateChatRoomName(groupName);

			boolean created = this.createPrivateRoom(chatRoomName);

			if (created) {
				JID roomJID = DbManager.getInstance().getChatRoomJID(chatRoomName);

				if (roomJID != null) {
					this.updateRoomSubject(roomJID, groupName);

					if (!CollectionUtils.isNullOrEmpty(members)) {
						for (JID memberJID : members) {
							this.sendAddChatRoomMemberRequest(roomJID, memberJID);
						}
					}
				}
			}

			return created;

		} catch (Exception e) {
			String errorMessage = "Failed to Create private group with name " + groupName;

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}

	}

	/**
	 * Create public room with given room name. Any one can enter in this room
	 * using {@link UserManager#sendJoinChatRoomRequest(JID, String)}. Only room
	 * admin and owner can add or remove other members.
	 *
	 * @param groupName
	 * @param members group members list
	 * @return
	 * @throws StringflowErrorException
	 */
	public boolean createPublicGroup(String groupName, List<JID> members) throws StringflowErrorException {

		try {
			String chatRoomName = generateChatRoomName(groupName);

			boolean created = this.createPublicRoom(chatRoomName);

			if (created) {
				JID roomJID = DbManager.getInstance().getChatRoomJID(chatRoomName);

				if (roomJID != null) {
					this.updateRoomSubject(roomJID, groupName);

					if (!CollectionUtils.isNullOrEmpty(members)) {
						for (JID memberJID : members) {
							this.sendAddChatRoomMemberRequest(roomJID, memberJID);
						}
					}
				}
			}

			return created;
		} catch (Exception e) {
			String errorMessage = "Failed to Create public group with name " + groupName;

			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	private String generateChatRoomName(String groupName) {
		return groupName + "-" + UUIDGenerator.secureId();
	}

	@Override
	public boolean sendLeaveChatRoomRequest(JID roomJID) {
		try {
			boolean requestSent = super.sendLeaveChatRoomRequest(roomJID);

			if (requestSent) {
				DbManager.getInstance().removeRoomMember(roomJID.getBareJID(),
						Platform.getInstance().getUserJID().getBareJID());
			}

			return requestSent;
		} catch (Exception e) {
			String errorMessage = "Failed to Send leave group request for group JID : " + roomJID.getBareJID();
			LOGGER.log(Level.WARNING, errorMessage, e);
			return false;
		}
	}

	@Override
	public boolean updateRoomSubject(JID roomJID, String subject) {
		boolean requestSent = super.updateRoomSubject(roomJID, subject);

		if (requestSent) {
			try {

				DbManager.getInstance().updateChatRoomSubject(roomJID.getBareJID(), subject);

			} catch (DbException e) {
				String errorMessage = "Failed to update chat room subject in database for group JID : "
						+ roomJID.getBareJID();
				LOGGER.log(Level.WARNING, errorMessage, e);
				return false;
			}
		}

		return requestSent;
	}

	/**
	 * Reloads logged in user's Profile data from server.
	 */
	public void reloadUserData() {
		this.getUserProfileData();
	}

	/**
	 * Reloads user Profile data for given user jid from server.
	 */
	public void reloadUserData(JID userJID) {
		this.getUserProfileData(userJID);
	}

	/**
	 * It will fetch logged in user's data from server. it refers
	 * XEP-0054(vcard-temp) to access user profile related data.
	 *
	 * @return
	 */
	@Override
	public UserProfileData getUserProfileData() {
		UserProfileData userProfileData = super.getUserProfileData();

		if (userProfileData != null) {
			try {

				DbManager.getInstance().addOrUpdateUserProfileData(userProfileData);

			} catch (DbException e) {
				String errorMessage = "Failed to store user profile data";
				LOGGER.log(Level.WARNING, errorMessage, e);
			}
		}

		return userProfileData;
	}

	/**
	 * It will fetch user profile data for given user jid from server. it refers
	 * XEP-0054(vcard-temp) to access user profile related data.
	 *
	 * @return
	 */
	@Override
	public UserProfileData getUserProfileData(JID userJID) {
		UserProfileData userProfileData = super.getUserProfileData(userJID);

		if (userProfileData != null) {
			try {

				DbManager.getInstance().addOrUpdateUserProfileData(userProfileData);

			} catch (DbException e) {
				String errorMessage = "Failed to store user profile data";
				LOGGER.log(Level.WARNING, errorMessage, e);
			}
		}

		return userProfileData;
	}

	/**
	 * return true if user data is updated successfully. Please refer V-Card
	 * XEP-0054.
	 *
	 * @param userProfileData
	 */
	@Override
	public boolean updateUserProfileData(UserProfileData userProfileData) {
		boolean isUpdated = super.updateUserProfileData(userProfileData);

		if (isUpdated) {
			userProfileData.setJabberId(Platform.getInstance().getUserJID());
			try {

				DbManager.getInstance().addOrUpdateUserProfileData(userProfileData);

			} catch (DbException e) {
				String errorMessage = "Failed to store updated user profile data";
				LOGGER.log(Level.WARNING, errorMessage, e);
			}
		}

		return isUpdated;
	}

	/**
	 * User it to change user avatar.
	 *
	 * <p>
	 * The image SHOULD use less than eight kilobytes (8k) of data; And it's
	 * height and width SHOULD be between thirty-two (32) and ninety-six (96)
	 * pixels; the recommended size is sixty-four (64) pixels high and
	 * sixty-four (64) pixels wide.
	 * </p>
	 *
	 * <p>
	 * We Only verify total size of file which should be less than eight
	 * kilobytes (8k). and other verifications like height, width, pixels client
	 * have to do.
	 * </p>
	 *
	 * Please refer XEP-0153: vCard-Based Avatars.
	 *
	 * @param file
	 * @param imageType
	 * @return true if user avtar is changes successfully.
	 */
	public boolean changeUserAvatar(final File file, final String imageType) {
		boolean isChanged = changeUserAvatar(file, imageType);

		if (isChanged) {
			// DbManager.getInstance().updateUserProfileImage(Platform.getInstance().getUserJID().getBareJID(),
			// file, imageType);
		}

		return isChanged;
	}

	/**
	 * It will return cached user profile data from local DB. To refresh data
	 * first use {@link #reloadUserData()}. which will reload user data from
	 * server.
	 *
	 * @return
	 * @throws StringflowErrorException
	 */
	public UserProfileData getCachedUserProfileData() throws StringflowErrorException {
		try {
			return DbManager.getInstance().getUserProfileData(Platform.getInstance().getUserJID().getBareJID());
		} catch (DbException e) {
			String errorMessage = "Failed to get user profile data due to database operation failure";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * It will return cached user data for given userJID from local DB. To
	 * refresh data first use {@link #reloadUserData(JID)}. which will reload
	 * user data from server.
	 *
	 * @return
	 * @throws StringflowErrorException
	 */
	public UserProfileData getCachedUserProfileData(JID userJID) throws StringflowErrorException {
		try {
			return DbManager.getInstance().getUserProfileData(userJID.getBareJID());
		} catch (DbException e) {
			String errorMessage = "Failed to get user profile data due to database operation failure";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * It will return cached user avatar from local DB. To refresh data first
	 * use {@link #reloadUserData()}. which will reload user data from server.
	 *
	 * @param useeJID
	 * @return avatar
	 * @throws StringflowErrorException
	 */
	public InputStream getUserAvatar(JID userJID) throws StringflowErrorException {
		try {

			return DbManager.getInstance().getUserAvatar(userJID.getBareJID());

		} catch (DbException e) {
			String errorMessage = "Failed to get user Avatar due to database operation failure";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * It will return cached user avatar from local DB. To refresh data first
	 * use {@link #reloadUserData()}. which will reload user data from server.
	 *
	 * @param useeJID
	 * @return avatar
	 * @throws StringflowErrorException
	 */
	public InputStream getUserAvatar(String userJID) throws StringflowErrorException {
		try {

			return DbManager.getInstance().getUserAvatar(userJID);

		} catch (DbException e) {
			String errorMessage = "Failed to get user Avatar due to database operation failure";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new StringflowErrorException(errorMessage, e);
		}
	}

	/**
	 * Discard Device Token
	 * 
	 * @throws StringflowErrorException
	 */
	private void discardDeviceToken() throws StringflowErrorException {
		PushNotificationService notificationSrevice = SFSDKProperties.getInstance().getNotificationService();

		if (notificationSrevice != null) {
			String deviceToken = SFSDKProperties.getInstance().getDeviceToken();

			if (!StringUtils.isNullOrEmpty(deviceToken)) {
				this.discardDeviceToken(deviceToken, notificationSrevice, DeviceType.ANDROID);
			}
		}
	}

	/**
	 * This is used to logout user. It will handle all tasks required at sdk
	 * level.
	 * 
	 * @throws StringflowErrorException
	 */
	public void logoutUser() throws StringflowErrorException {
		this.discardDeviceToken();
		this.shutdownSDK();
	}

	/**
	 * Shutdown all android sdk.
	 * 
	 * @throws StringflowErrorException
	 */
	public void shutdownSDK() throws StringflowErrorException {
		Platform.getInstance().shutdown();
		GenieSDKInitializer.unloadSdk();
	}

}
