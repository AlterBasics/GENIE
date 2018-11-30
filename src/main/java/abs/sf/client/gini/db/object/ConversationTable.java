package abs.sf.client.gini.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ConversationTable extends DatabaseTable {
    public static final String TABLE_NAME = "conversation";

    public static final String COLUMN_CONVERSATION_ID = "conversation_id";
    public static final String COLUMN_PEER_JID = "peer_jid"; //bare jid
    public static final String COLUMN_LAST_CHATLINE = "last_chatline";
    public static final String COLUMN_LAST_CHATLINE_TYPE = "last_chatline_type";
    public static final String COLUMN_LAST_UPDATE_TIME = "update_time";
    public static final String COLUMN_UNREAD_CHATLINE_COUNT = "unread_chatline_count";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_CONVERSATION_ID + " INT auto_increment primary key, "
            + COLUMN_PEER_JID + " TEXT not null, "
            + COLUMN_LAST_CHATLINE + " TEXT not null,"
            + COLUMN_LAST_CHATLINE_TYPE + " TEXT not null,"
            + COLUMN_LAST_UPDATE_TIME + " BIGINT, "
            + COLUMN_UNREAD_CHATLINE_COUNT + " INT not null "
            + ");";

    public ConversationTable() {
        super(TABLE_NAME);
    }

    @Override
    public void create(Connection conn) throws SQLException {
		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate(SQL_CREATE);
		}	
    }

}
