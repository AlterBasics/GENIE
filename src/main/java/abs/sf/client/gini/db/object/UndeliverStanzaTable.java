package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UndeliverStanzaTable extends DatabaseTable {
    public static final String TABLE_NAME = "undeliver_stanza";

    public static final String COL_UUID = "uuid";
    public static final String COL_STANZA = "stanza";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COL_UUID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_STANZA + " BLOB not null "
            + ");";


    public UndeliverStanzaTable() {
        super(TABLE_NAME);
    }

    public void create(SQLiteDatabase database) throws SQLException {
        Log.d(UndeliverStanzaTable.class.getCanonicalName(), "creating Undeliver_Stanza Table ");
        database.execSQL(SQL_CREATE);
    }
}
