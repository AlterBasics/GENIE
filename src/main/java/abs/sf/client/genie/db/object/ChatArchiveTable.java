package abs.sf.client.genie.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ChatArchiveTable extends DatabaseTable {
	public static final String TABLE_NAME = "chat_archive";

	public static final String COLUMN_ID = "uuid";
	public static final String COLUMN_JID = "jid";
	public static final String COLUMN_MESSAGE = "content";
	public static final String COLUMN_CREATE_TIME = "create_time";

	// Database creation SQL statement
	private static final String SQL_CREATE = "create table " + TABLE_NAME + "(" 
	        + COLUMN_ID + " INT auto_increment primary key , " 
			+ COLUMN_JID + " VARCHAR(255) not null, "
	        + COLUMN_MESSAGE + " TEXT not null, " 
			+ COLUMN_CREATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " + ")";

	public ChatArchiveTable() {
		super(TABLE_NAME);
	}

	public void create(Connection conn) throws SQLException {
		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate(SQL_CREATE);
		}
	}

}