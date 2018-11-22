package abs.sf.client.gini.db.h2;

import abs.sf.client.gini.db.object.ChatRoomMemberTable;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.MediaStoreTable;
import abs.sf.client.gini.db.object.PresenceTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.db.object.UndeliverStanzaTable;
import abs.sf.client.gini.db.object.UserProfileTable;
import abs.sf.client.gini.messaging.ChatLine;

/**
 * All the SQL queries used across the SDK.
 */
public class SQLQuery {
    public static final String FETCH_CONVERSATIONS = "SELECT " +
        "cnv." + ConversationTable.COLUMN_CONVERSATION_ID +
        ",cnv." + ConversationTable.COLUMN_PEER_JID +
        ",cnv." + ConversationTable.COLUMN_LAST_CHATLINE +
        ",cnv." + ConversationTable.COLUMN_LAST_CHATLINE_TYPE +
        ",cnv." + ConversationTable.COLUMN_LAST_UPDATE_TIME +
        ",cnv." + ConversationTable.COLUMN_UNREAD_CHATLINE_COUNT +
        ",rst." + RosterTable.COLUMN_NAME +
        ",rst." + RosterTable.COLUMN_ROOM_SUBJECT +
        ",rst." + RosterTable.COLUMN_IS_GROUP +
        " FROM " + ConversationTable.TABLE_NAME + " cnv " +
        " INNER JOIN " + RosterTable.TABLE_NAME + " rst " +
        " ON cnv." + ConversationTable.COLUMN_PEER_JID +
        " = rst." + RosterTable.COLUMN_JID +
        " ORDER BY cnv."+ ConversationTable.COLUMN_LAST_UPDATE_TIME + " DESC ";


    public static final String FETCH_CONVERSATION_COUNT = "Select count(*) " +
            " FROM " + ConversationTable.TABLE_NAME +
            " WHERE " + ConversationTable.COLUMN_PEER_JID + " = ?";

    public static final String FETCH_MESSAGE_UUID = "Select " + ChatStoreTable.COL_UUID +
            " FROM " + ChatStoreTable.TABLE_NAME +
            " WHERE " + ChatStoreTable.COL_MESSAGE_ID + " = ?";

    public static final String FETCH_MESSAGE_DELIVERY_STATUS = "Select " + ChatStoreTable.COL_DELIVERY_STATUS +
            " FROM " + ChatStoreTable.TABLE_NAME +
            " WHERE " + ChatStoreTable.COL_MESSAGE_ID + " = ?";

    public static final String FETCH_CHAT_LINE_COUNT = "Select count(*) " +
            " FROM " + ChatStoreTable.TABLE_NAME +
            " WHERE " + ChatStoreTable.COL_MESSAGE_ID + " = ? " +
            " AND " + ChatStoreTable.COL_PEER_JID + " = ? ";

    public static final String FETCH_UNREAD_CHATLINE_COUNT = "SELECT " +
            ConversationTable.COLUMN_UNREAD_CHATLINE_COUNT +
            " FROM " + ConversationTable.TABLE_NAME +
            " WHERE " + ConversationTable.COLUMN_PEER_JID + "= ?";

    public static final String FETCH_CONVERSATION_CHAT_LINES = "SELECT  cst.* " +
            " FROM " + ChatStoreTable.TABLE_NAME + " cst " +
            " WHERE  cst." + ChatStoreTable.COL_PEER_JID + " = ? " +
            " ORDER BY cst." + ChatStoreTable.COL_CREATE_TIME + " ASC ";

    public static final String FETCH_UNREAD_CONVERSATION_CHAT_LINES = "SELECT  cst.* " +
            " FROM " + ChatStoreTable.TABLE_NAME + " cst " +
            " WHERE  cst." + ChatStoreTable.COL_PEER_JID + " = ? " +
            " AND  cst." + ChatStoreTable.COL_HAVE_SEAN + " = 0 " +
            " ORDER BY cst." + ChatStoreTable.COL_CREATE_TIME + " ASC ";

    public static final String FETCH_UNDELIVERED_CHAT_LINES = "SELECT  * FROM " + ChatStoreTable.TABLE_NAME +
            " WHERE " + ChatStoreTable.COL_DELIVERY_STATUS + " = " + ChatLine.MessageStatus.NOT_DELIVERED_TO_SERVER.getValue() + " " +
            " AND " + ChatStoreTable.COL_DIRECTION + " = " + ChatLine.Direction.SEND.val();

    public static final String FETCH_ROSTER_ITEMS = " SELECT  * " +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_IS_GROUP + " = 0";

    public static final String FETCH_USER_NAME = " Select " + RosterTable.COLUMN_NAME +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_JID + " = ? ";

    public static final String FETCH_CHAT_ROOM_SUBJECT = " Select " + RosterTable.COLUMN_ROOM_SUBJECT +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_JID + " = ? ";

    public static final String FETCH_ROOM_MEMBER_NICK_NAME = " Select " + ChatRoomMemberTable.COLUMN_MEMBER_NICK_NAME +
            " FROM " + ChatRoomMemberTable.TABLE_NAME +
            " WHERE " + ChatRoomMemberTable.COLUMN_MEMBER_JID + " = ? " +
            " AND " + ChatRoomMemberTable.COLUMN_ROOM_JID + " = ? ";

    public static final String FETCH_ROOM_MEMBER_JID = " Select " + ChatRoomMemberTable.COLUMN_MEMBER_JID +
            " FROM " + ChatRoomMemberTable.TABLE_NAME +
            " WHERE " + ChatRoomMemberTable.COLUMN_ROOM_JID + " = ? " +
            " AND " + ChatRoomMemberTable.COLUMN_MEMBER_NICK_NAME + " = ? ";

    public static final String FETCH_ROSTER_ITEM_COUNT = "Select count(*) " +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_JID + " = ? ";

    public static final String FETCH_GROUP_COUNT = "Select count(*) " +
            "  FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_JID + " = ? " +
            " AND " + RosterTable.COLUMN_IS_GROUP + " = 1 ";

    public static final String FETCH_CHAT_ROOM_COUNT = "Select count(*) " +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_JID + " = ? " +
            " AND " + RosterTable.COLUMN_IS_GROUP + " = 1";

    public static final String DELETE_ROOM_MEMBER = "DELETE " +
            " FROM " + ChatRoomMemberTable.TABLE_NAME +
            " WHERE " + ChatRoomMemberTable.COLUMN_MEMBER_JID + " = ? " +
            " AND " + ChatRoomMemberTable.COLUMN_ROOM_JID + " = ?";

    public static final String FETCH_CHAT_ROOM_DETAILS = "SELECT " +
            RosterTable.COLUMN_JID +
            ", " + RosterTable.COLUMN_NAME +
            ", " + RosterTable.COLUMN_ROOM_SUBJECT +
            ", " + RosterTable.COLUMN_ACCESS_MODE +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_JID + " = ? " +
            " AND " + RosterTable.COLUMN_IS_GROUP + " = 1";

    public static final String FETCH_CHAT_ROOM_JID = "SELECT " +
            RosterTable.COLUMN_JID +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_NAME + " = ? " +
            " AND " + RosterTable.COLUMN_IS_GROUP + " = 1";

    public static final String FETCH_CHAT_ROOMS = "SELECT " +
            RosterTable.COLUMN_JID +
            ", " + RosterTable.COLUMN_NAME +
            ", " + RosterTable.COLUMN_ROOM_SUBJECT +
            ", " + RosterTable.COLUMN_ACCESS_MODE +
            " FROM " + RosterTable.TABLE_NAME +
            " WHERE " + RosterTable.COLUMN_IS_GROUP + " = 1";

    public static final String FETCH_CHAT_ROOM_MEMBERS = "SELECT " +
            ChatRoomMemberTable.COLUMN_MEMBER_JID +
            ", " + ChatRoomMemberTable.COLUMN_MEMBER_NICK_NAME +
            ", " + ChatRoomMemberTable.COLUMN_AFFILATION +
            ", " + ChatRoomMemberTable.COLUMN_ROLE +
            " FROM " + ChatRoomMemberTable.TABLE_NAME +
            " WHERE " + ChatRoomMemberTable.COLUMN_ROOM_JID + " = ?";

    public static final String FETCH_PRESENCE_COUNT = "SELECT count(*)" +
            " FROM " + PresenceTable.TABLE_NAME +
            " WHERE " + PresenceTable.COLUMN_JID + " = ?";

    public static final String FETCH_PRESENCE_DETAILS = "SELECT " +
            PresenceTable.COLUMN_JID +
            ", " + PresenceTable.COLUMN_PRESNCE +
            ", " + PresenceTable.COLUMN_MOOD +
            ", " + PresenceTable.COLUMN_STATUS +
            ", " + PresenceTable.COLUMN_LAST_UPDATE +
            " FROM " + PresenceTable.TABLE_NAME +
            " WHERE " + PresenceTable.COLUMN_JID + " = ?";

    public static final String FETCH_PRESENCE_STATUS = "SELECT " +
            PresenceTable.COLUMN_PRESNCE +
            " FROM " + PresenceTable.TABLE_NAME +
            " WHERE " + PresenceTable.COLUMN_JID + " = ?";

    public static final String FETCH_CHAT_ROOM_MEMBER_COUNT = "SELECT count(*)" +
            " FROM " + ChatRoomMemberTable.TABLE_NAME +
            " WHERE " + ChatRoomMemberTable.COLUMN_MEMBER_JID + " = ? " +
            " AND " + ChatRoomMemberTable.COLUMN_ROOM_JID + " =? ";

    public static final String FETCH_MEDIA_DETAILS_BY_UUID = "SELECT " +
            MediaStoreTable.COL_UUID +
            ", " + MediaStoreTable.COL_MEDIA_ID +
            ", " + MediaStoreTable.COL_MEDIA_THUMB +
            ", " + MediaStoreTable.COL_MEDIA_PATH +
            ", " + MediaStoreTable.COL_CONTENT_TYPE +
            " FROM " + MediaStoreTable.TABLE_NAME +
            " WHERE " + MediaStoreTable.COL_UUID + " = ?";

    public static final String FETCH_MEDIA_DETAILS_BY_MEDIA_ID = "SELECT " +
            MediaStoreTable.COL_UUID +
            ", " + MediaStoreTable.COL_MEDIA_ID +
            ", " + MediaStoreTable.COL_MEDIA_THUMB +
            ", " + MediaStoreTable.COL_MEDIA_PATH +
            ", " + MediaStoreTable.COL_CONTENT_TYPE +
            " FROM " + MediaStoreTable.TABLE_NAME +
            " WHERE " + MediaStoreTable.COL_MEDIA_ID + " = ?";

    public static final String FETCH_MEDIA_PATH_BY_UUID = "SELECT " +
            ", " + MediaStoreTable.COL_MEDIA_PATH +
            " FROM " + MediaStoreTable.TABLE_NAME +
            " WHERE " + MediaStoreTable.COL_UUID + " = ?";

    public static final String FETCH_MEDIA_PATH_BY_MEDIA_ID = "SELECT " +
            ", " + MediaStoreTable.COL_MEDIA_PATH +
            " FROM " + MediaStoreTable.TABLE_NAME +
            " WHERE " + MediaStoreTable.COL_MEDIA_ID + " = ?";

    public static final String DELETE_FIRST_UNDELIVERED_STANZAS = "DELETE " +
            " FROM " + UndeliverStanzaTable.TABLE_NAME +
            " WHERE " + UndeliverStanzaTable.COL_UUID + " IN " +
            "  (SELECT " + UndeliverStanzaTable.COL_UUID +
            " FROM " + UndeliverStanzaTable.TABLE_NAME +
            " order by " + UndeliverStanzaTable.COL_UUID +
            " LIMIT ?)";

    public static final String FETCH_ALL_UNDELIVERD_STANZAS = "SELECT " +
            UndeliverStanzaTable.COL_STANZA +
            " FROM " + UndeliverStanzaTable.TABLE_NAME;

    public static final String FETCH_USER_PROFILE_COUNT = "SELECT count(*)" +
            " FROM " + UserProfileTable.TABLE_NAME +
            " WHERE " + UserProfileTable.COLUMN_JID + " = ?";

    public static final String FETCH_USER_PROFILE_DATA = "SELECT " +
            UserProfileTable.COLUMN_JID +
            ", " + UserProfileTable.COLUMN_FIRST_NAME +
            ", " + UserProfileTable.COLUMN_MIDDLE_NAME +
            ", " + UserProfileTable.COLUMN_LAST_NAME +
            ", " + UserProfileTable.COLUMN_NICK_NAME +
            ", " + UserProfileTable.COLUMN_EMAIL +
            ", " + UserProfileTable.COLUMN_PHONE +
            ", " + UserProfileTable.COLUMN_GENDER +
            ", " + UserProfileTable.COLUMN_BDAY +
            ", " + UserProfileTable.COLUMN_ADDRESS_HOME +
            ", " + UserProfileTable.COLUMN_ADDRESS_STREET +
            ", " + UserProfileTable.COLUMN_ADDRESS_LOCALITY +
            ", " + UserProfileTable.COLUMN_ADDRESS_CITY +
            ", " + UserProfileTable.COLUMN_ADDRESS_STATE +
            ", " + UserProfileTable.COLUMN_ADDRESS_COUNTRY +
            ", " + UserProfileTable.COLUMN_ADDRESS_PCODE +
            ", " + UserProfileTable.COLUMN_ABOUT +
            " FROM " + UserProfileTable.TABLE_NAME +
            " WHERE " + UserProfileTable.COLUMN_JID + " = ?";

    public static final String FETCH_USER_PROFILE_AVATAR = "SELECT " +
            UserProfileTable.COLUMN_AVATAR +
            ", " + UserProfileTable.COLUMN_AVATAR_MEDIA_TYPE +
            " FROM " + UserProfileTable.TABLE_NAME +
            " WHERE " + UserProfileTable.COLUMN_JID + " = ?";
    
    public static final String SQL_INSERT_CONVERSATION = "INSERT INTO " +
    		ConversationTable.TABLE_NAME +
    		" (" + ConversationTable.COLUMN_PEER_JID+
            ", " + ConversationTable.COLUMN_LAST_CHATLINE +
            ", " + ConversationTable.COLUMN_LAST_CHATLINE_TYPE +
            ", " + ConversationTable.COLUMN_LAST_UPDATE_TIME +
            ", " + ConversationTable.COLUMN_UNREAD_CHATLINE_COUNT +
            " ) VALUES (?, ?, ?, ?, ?)";
        
    
    public static final String SQL_UPDATE_CONVERSATION = "UPDATE " +
    		ConversationTable.TABLE_NAME + " SET " +
            ConversationTable.COLUMN_LAST_CHATLINE + " = ?, " +
            ConversationTable.COLUMN_LAST_CHATLINE_TYPE + " = ?, " +
            ConversationTable.COLUMN_LAST_UPDATE_TIME + " = ?, " + 
            ConversationTable.COLUMN_UNREAD_CHATLINE_COUNT + " = ? " +
            " WHERE " + ConversationTable.COLUMN_PEER_JID + " = ?";
    
    public static final String SQL_UPDATE_UNREAD_CONVERSATION_COUNT = "UPDATE " +
    		ConversationTable.TABLE_NAME + " SET " +
            ConversationTable.COLUMN_UNREAD_CHATLINE_COUNT + " = ? " +
            " WHERE " + ConversationTable.COLUMN_PEER_JID + " = ?";
    
    public static final String SQL_INSERT_CHATLINE_TO_CHATSTORE =  "INSERT INTO " +
    		ChatStoreTable.TABLE_NAME +
    		" (" + ChatStoreTable.COL_CONVERSATION_ID+ 
            ", " + ChatStoreTable.COL_MESSAGE_ID +
            ", " + ChatStoreTable.COL_PEER_JID +
            ", " + ChatStoreTable.COL_PEER_RESOURCE +
            ", " + ChatStoreTable.COL_DIRECTION +
            ", " + ChatStoreTable.COL_CHATLINE +
            ", " + ChatStoreTable.COL_CHATLINE_TYPE +
            ", " + ChatStoreTable.COL_CHATLINE_CONTENT_ID +
            ", " + ChatStoreTable.COL_CREATE_TIME +
            ", " + ChatStoreTable.COL_DELIVERY_STATUS +
            ", " + ChatStoreTable.COL_IS_MARKABLE +
            ", " + ChatStoreTable.COL_HAVE_SEAN +
            ", " + ChatStoreTable.COL_IS_CSN_ACTIVE +
            " ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    
    public static final String SQL_UPDATE_MESSAGE_DELIVERY_STATUS = "UPDATE " +
    		ChatStoreTable.TABLE_NAME + " SET " +
            ChatStoreTable.COL_DELIVERY_STATUS + " = ? " +
            " WHERE " + ChatStoreTable.COL_MESSAGE_ID + " = ?";  
    
    public static final String SQL_IS_MESSAGE_VIEWED = "UPDATE " +
    		ChatStoreTable.TABLE_NAME + " SET " +
            ChatStoreTable.COL_HAVE_SEAN + " = ? " +
            " WHERE " + ChatStoreTable.COL_MESSAGE_ID + " = ?";  

}
