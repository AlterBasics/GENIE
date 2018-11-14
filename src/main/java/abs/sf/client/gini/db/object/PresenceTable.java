package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PresenceTable extends DatabaseTable {
    private static final String TAG = "USER_PRESENCE";

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
            + COLUMN_ID + " INTEGER primary key autoincrement, "
            + COLUMN_JID + " TEXT not null, "
            + COLUMN_PRESNCE + " TEXT, "
            + COLUMN_MOOD + " TEXT, "
            + COLUMN_STATUS + " TEXT, "
            + COLUMN_LAST_UPDATE + " INTEGER not null"
            + ");";

    public PresenceTable(){
        super(TABLE_NAME);
    }

    public void create(SQLiteDatabase database) throws SQLException {
        Log.d(PresenceTable.class.getCanonicalName(), "creating User presence table");
        database.execSQL(SQL_CREATE);
    }

}
