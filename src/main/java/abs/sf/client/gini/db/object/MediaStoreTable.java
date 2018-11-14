package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MediaStoreTable extends DatabaseTable {
    public static final String TABLE_NAME = "media_store";

    public static final String COL_UUID = "uuid";
    public static final String COL_MEDIA_ID = "media_id";
    public static final String COL_MEDIA_THUMB = "media_thumb";
    public static final String COL_MEDIA_PATH = "media_path";
    public static final String COL_CONTENT_TYPE = "content_type";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COL_UUID + " INTEGER primary key AUTOINCREMENT, "
            + COL_MEDIA_ID + " TEXT UNIQUE not null, "
            + COL_MEDIA_THUMB + " BLOB, "
            + COL_MEDIA_PATH + " TEXT,"
            + COL_CONTENT_TYPE + " TEXT "
            + ");";

    public MediaStoreTable() {
        super(TABLE_NAME);
    }

    public void create(SQLiteDatabase database) throws SQLException {
        database.execSQL(SQL_CREATE);
    }
}
