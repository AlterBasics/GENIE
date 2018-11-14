package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PollResponseTable extends DatabaseTable{
    public static final String TABLE_NAME = "poll_response";

    public static final String COL_UUID = "uuid";
    public static final String COL_POLL_ID = "poll_id";
    public static final String COL_RESPONSE = "response";
    public static final String COL_RESPONSOR_JID= "responsor_jid";

    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COL_UUID + " INTEGER primary key AUTOINCREMENT,"
            + COL_POLL_ID + " INTEGER , "
            + COL_RESPONSE + " TEXT not null, "
            + COL_RESPONSOR_JID + " TEXT "
            + ");";

    public PollResponseTable() {
        super(TABLE_NAME);
    }

    public void create(SQLiteDatabase database) throws SQLException {
        database.execSQL(SQL_CREATE);
    }
}
