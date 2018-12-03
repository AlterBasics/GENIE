package abs.sf.client.gini.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PresenceTable extends DatabaseTable {
    public static final String TAG = "USER_PRESENCE";

    public static final String TABLE_NAME = "presence";

    public static final String COLUMN_ID = "uuid";
    public static final String COLUMN_JID = "jid";
    public static final String COLUMN_PRESNCE = "presence";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_LAST_UPDATE = "last_update";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " INT auto_increment primary key, "
            + COLUMN_JID + " TEXT not null, "
            + COLUMN_PRESNCE + " TEXT, "
            + COLUMN_MOOD + " TEXT, "
            + COLUMN_STATUS + " TEXT, "
            + COLUMN_LAST_UPDATE + " BIGINT not null"
            + ");";

    public PresenceTable(){
        super(TABLE_NAME);
    }

    @Override
    public void create(Connection conn) throws SQLException {
		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate(SQL_CREATE);
		}	
    }

}
