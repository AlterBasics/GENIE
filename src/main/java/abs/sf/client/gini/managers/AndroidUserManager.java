package abs.sf.client.gini.managers;

import java.util.List;

import abs.ixi.client.DeviceType;
import abs.ixi.client.PushNotificationService;
import abs.ixi.client.UserManager;
import abs.ixi.client.core.Callback;
import abs.ixi.client.core.Platform;
import abs.ixi.client.io.StreamNegotiator;
import abs.ixi.client.io.XMPPStreamManager;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.util.UUIDGenerator;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.ixi.client.xmpp.packet.Roster;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.utils.SDKLoader;
import abs.sf.client.gini.utils.SharedPrefProxy;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AndroidUserManager extends UserManager {
    public AndroidUserManager(XMPPStreamManager streamManager) {
        super(streamManager);
    }

    /**
     * Check given jid is a chat room (group) {@link JID}.
     *
     * @param jid
     * @return
     */
    public boolean checkIsChatRoom(JID jid) {
        return DbManager.getInstance().isRosterGroup(jid.getBareJID());
    }

    /**
     * Check that given contact is a member of a given group.
     *
     * @param userJID
     * @param groupJID
     * @return
     */
    public boolean checkIsChatRoomMember(JID userJID, JID groupJID) {
        return DbManager.getInstance().isChatRoomMember(groupJID, userJID);
    }

    /**
     * @return All user contact members and groups list in the form of {@link abs.ixi.client.xmpp.packet.Roster.RosterItem}'s list.
     */
    public List<Roster.RosterItem> getRosterItemList() {
        return DbManager.getInstance().getRosterList();
    }

    /**
     * @param roomJID
     * @return all details of given {@link JID}'s group.
     */
    public ChatRoom getChatRoomDetails(JID roomJID) {
        return DbManager.getInstance().getChatRoomDetails(roomJID.getBareJID());
    }

    /**
     * @param itemJID
     * @return Actual name of given {@link JID}'s user.
     */
    public String getRosterItemName(JID itemJID) {
        return DbManager.getInstance().getRosterItemName(itemJID.getBareJID());
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
                //For now do nothing
            }
        });
    }

    /**
     * Create private room with given room name. No one can enter in this room
     * using {@link UserManager#sendJoinChatRoomRequest(JID, String)}. Only room
     * admin and owner can add or remove other members.
     *
     * @param groupName
     * @param members   group members list
     * @return
     */
    public boolean createPrivateGroup(String groupName, List<JID> members) {

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
    }

    /**
     * Create public room with given room name. Any one can enter in this room
     * using {@link UserManager#sendJoinChatRoomRequest(JID, String)}. Only room
     * admin and owner can add or remove other members.
     *
     * @param groupName
     * @param members   group members list
     * @return
     */
    public boolean createPublicGroup(String groupName, List<JID> members) {

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
    }


    private String generateChatRoomName(String groupName) {
        return groupName + "-" + UUIDGenerator.secureId();
    }

    @Override
    public boolean sendLeaveChatRoomRequest(JID roomJID) {
        boolean requestSent =  super.sendLeaveChatRoomRequest(roomJID);

        if(requestSent) {
            DbManager.getInstance().removeRoomMember(roomJID.getBareJID(), Platform.getInstance().getUserJID().getBareJID());
        }

        return requestSent;
    }

    @Override
    public boolean updateRoomSubject(JID roomJID, String subject) {
        boolean requestSent =  super.updateRoomSubject(roomJID, subject);

        if(requestSent) {
            DbManager.getInstance().updateChatRoomSubject(roomJID.getBareJID(), subject);
        }

        return requestSent;
    }

    /**
     *
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
     * It will fetch logged in user's data from server. it refers XEP-0054(vcard-temp) to access
     * user profile related data.
     *
     * @return
     */
    @Override
    public UserProfileData getUserProfileData() {
        UserProfileData userProfileData = super.getUserProfileData();

        if (userProfileData != null) {
            DbManager.getInstance().addOrUpdateUserProfileData(userProfileData);
        }

        return userProfileData;
    }

    /**
     * It will fetch user profile data for given user jid from server. it refers XEP-0054(vcard-temp) to access
     * user profile related data.
     *
     * @return
     */
    @Override
    public UserProfileData getUserProfileData(JID userJID) {
        UserProfileData userProfileData = super.getUserProfileData(userJID);

        if (userProfileData != null) {
            DbManager.getInstance().addOrUpdateUserProfileData(userProfileData);
        }

        return userProfileData;
    }

    /**
     * It will return cached user profile data from local DB.
     * To refresh data first use {@link #reloadUserData()}. which will reload user data from server.
     *
     *
     * @return
     */
    public UserProfileData getCachedUserProfileData() {
        return DbManager.getInstance().getUserProfileData(Platform.getInstance().getUserJID().getBareJID());
    }

    /**
     * It will return cached user data for given userJID from local DB.
     * To refresh data first use {@link #reloadUserData(JID)}. which will reload user data from server.
     *
     *
     * @return
     */
    public UserProfileData getCachedUserProfileData(JID userJID) {
        return DbManager.getInstance().getUserProfileData(userJID.getBareJID());
    }
    /**
     * It will return cached user avtar from local DB.
     * To refresh data first use {@link #reloadUserData()}. which will reload user data from server.
     *
     * @param useeJID
     * @return avatar
     */
    public byte[] getUserAvatarBytes(JID useeJID) {
        return DbManager.getInstance().getUserAvatarBytes(useeJID.getBareJID());
    }

    /**
     * It will return cached user avtar from local DB.
     * To refresh data first use {@link #reloadUserData(JID)}. which will reload user data from server.
     *
     * @param useeJID
     * @return avatar
     */
    public Bitmap getUserAvatar(JID useeJID) {
        byte[] avtarBytes = DbManager.getInstance().getUserAvatarBytes(useeJID.getBareJID());

        if (avtarBytes != null) {
           return BitmapFactory.decodeByteArray(avtarBytes, 0, avtarBytes.length);
        }

        return null;
    }

    /**
     * After Sdk loading call this method to login.
     *
     * @param userName
     * @param password
     * @param domain
     * @param callback
     */
    public void loginUser(final String userName, final String password, final String domain,
                          final Callback<StreamNegotiator.NegotiationResult, Exception> callback) {

        SharedPrefProxy.getInstance().setDomainName(domain);

        this.login(userName, password, domain, callback);
    }


    /**
     * Discard Device Token
     */
    private void discardDeviceToken() {
        PushNotificationService notificationSrevice = SharedPrefProxy.getInstance().getNotificationService();

        if (notificationSrevice != null) {
            String deviceToken = SharedPrefProxy.getInstance().getDeviceToken();

            if (!StringUtils.isNullOrEmpty(deviceToken)) {
                this.discardDeviceToken(deviceToken, notificationSrevice, DeviceType.ANDROID);
            }
        }
    }

    /**
     * This is used to logout user. It will handle all tasks required at sdk level.
     */
    public void logoutUser() {
        this.discardDeviceToken();
        this.shutdownSDK();
    }

    /**
     * Shutdown all android sdk.
     */
    public void shutdownSDK() {
        Platform.getInstance().shutdown();
        SDKLoader.unloadSdk();
    }

}


