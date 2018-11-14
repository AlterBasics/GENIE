package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
            + COLUMN_ID + " INTEGER primary key autoincrement, "
            + COLUMN_JID + " TEXT not null, "
            + COLUMN_NAME + " TEXT,"
            + COLUMN_ROOM_SUBJECT + " TEXT, "
            + COLUMN_ACCESS_MODE + " TEXT, "
            + COLUMN_IS_GROUP + " INTEGER not null"
            + ");";

    public RosterTable() {
        super(TABLE_NAME);
    }

    public void create(SQLiteDatabase database) throws SQLException {
        Log.d(RosterTable.class.getCanonicalName(), "creating RosterTable table");
        database.execSQL(SQL_CREATE);
    }

}
