package abs.sf.client.gini.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UndeliverStanzaTable extends DatabaseTable {
    public static final String TABLE_NAME = "undeliver_stanza";

    public static final String COL_UUID = "uuid";
    public static final String COL_STANZA = "stanza";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COL_UUID + " INT auto_increment primary key, "
            + COL_STANZA + " BLOB not null "
            + ");";


    public UndeliverStanzaTable() {
        super(TABLE_NAME);
    }

    @Override
    public void create(Connection conn) throws SQLException {
		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate(SQL_CREATE);
		}	
    }
}
