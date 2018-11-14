package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ChatArchiveTable extends DatabaseTable {
    public static final String TABLE_NAME = "chat_archive";

    public static final String COLUMN_ID = "uuid";
    public static final String COLUMN_JID = "jid";
    public static final String COLUMN_MESSAGE = "content";
    public static final String COLUMN_CREATE_TIME = "create_time";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER primary key autoincrement, "
            + COLUMN_JID + " VARCHAR(255) not null, "
            + COLUMN_MESSAGE + " TEXT not null,"
            + COLUMN_CREATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ");";


    public ChatArchiveTable() {
        super(TABLE_NAME);
    }

    public void create(SQLiteDatabase database) throws SQLException {
        database.execSQL(SQL_CREATE);
    }

}