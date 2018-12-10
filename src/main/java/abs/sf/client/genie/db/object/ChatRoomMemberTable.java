package abs.sf.client.genie.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ChatRoomMemberTable extends DatabaseTable {
    public static final String TABLE_NAME = "chatroom_members";

    public static final String COLUMN_ID = "uuid";
    public static final String COLUMN_MEMBER_JID = "jid";
    public static final String COLUMN_MEMBER_NICK_NAME = "nick_name";
    public static final String COLUMN_AFFILATION = "affilation";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_ROOM_JID = "room_jid";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " INT auto_increment primary key, "
            + COLUMN_MEMBER_JID + " TEXT not null, "
            + COLUMN_ROOM_JID + " TEXT not null,"
            + COLUMN_MEMBER_NICK_NAME + " TEXT,"
            + COLUMN_AFFILATION + " TEXT ,"
            + COLUMN_ROLE + " TEXT "
            + ");";

    public ChatRoomMemberTable() {
        super(TABLE_NAME);
    }

    @Override
    public void create(Connection conn) throws SQLException {
		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate(SQL_CREATE);
		}	
    }

}
