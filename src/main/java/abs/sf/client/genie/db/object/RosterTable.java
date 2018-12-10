package abs.sf.client.genie.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class RosterTable extends DatabaseTable {
    public static final String TABLE_NAME = "roster";

    public static final String COLUMN_ID = "uuid";
    public static final String COLUMN_JID = "jid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ROOM_SUBJECT = "room_subject";
    public static final String COLUMN_ACCESS_MODE = "access_mode";
    public static final String COLUMN_IS_GROUP = "is_group";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " INT auto_increment primary key, "
            + COLUMN_JID + " TEXT not null, "
            + COLUMN_NAME + " TEXT,"
            + COLUMN_ROOM_SUBJECT + " TEXT, "
            + COLUMN_ACCESS_MODE + " TEXT, "
            + COLUMN_IS_GROUP + " INT "
            + ");";

    public RosterTable() {
        super(TABLE_NAME);
    }

    @Override
    public void create(Connection conn) throws SQLException {
		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate(SQL_CREATE);
		}	
    }

}
