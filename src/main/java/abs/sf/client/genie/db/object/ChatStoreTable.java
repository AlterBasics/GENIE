package abs.sf.client.genie.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The table which stores all the conversation data in database.
 * Currently all the data is stored with TEXT affinity however it
 * may require change to handle better the support for anything
 * outside BLP.
 */
public class ChatStoreTable extends DatabaseTable {
    public static final String TABLE_NAME = "chat_store";

    public static final String COL_UUID = "uuid";
    public static final String COL_CONVERSATION_ID = "conversation_id";
    public static final String COL_MESSAGE_ID = "message_id";
    public static final String COL_PEER_JID = "peer_jid"; //bare jid
    public static final String COL_PEER_RESOURCE = "peer_res";
    public static final String COL_DIRECTION = "direction";
    public static final String COL_CHATLINE = "chatline";
    public static final String COL_CHATLINE_TYPE = "chatline_type";
    public static final String COL_CHATLINE_CONTENT_ID = "chatline_content_id";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_DELIVERY_STATUS = "delivery_status";
    public static final String COL_IS_MARKABLE = "is_markable";
    public static final String COL_HAVE_SEAN = "have_sean";
    public static final String COL_IS_CSN_ACTIVE = "is_csn_active";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COL_UUID + " INT auto_increment primary key, "
            + COL_CONVERSATION_ID + " TEXT, "
            + COL_MESSAGE_ID + " TEXT not null, "
            + COL_PEER_JID + " TEXT not null, "
            + COL_PEER_RESOURCE + " TEXT, "
            + COL_DIRECTION + " INT not null,"
            + COL_CHATLINE + " TEXT,"
            + COL_CHATLINE_TYPE + " TEXT not null,"
            + COL_CHATLINE_CONTENT_ID + " INT not null,"
            + COL_CREATE_TIME + " BIGINT ,"
            + COL_DELIVERY_STATUS + " INT not null,"
            + COL_IS_MARKABLE + " INT not null,"
            + COL_HAVE_SEAN + " INT not null,"
            + COL_IS_CSN_ACTIVE + " INT not null"
            + ");";

    public ChatStoreTable() {
        super(TABLE_NAME);
    }

    @Override
    public void create(Connection conn) throws SQLException {
		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate(SQL_CREATE);
		}
    }

}
