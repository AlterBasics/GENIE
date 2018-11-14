package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ChatRoomMemberTable extends DatabaseTable {
    public static final String TABLE_NAME = "chatroom_members";

    public static final String COLUMN_ID = "uuid";
    public static final String COLUMN_MEMBER_JID = "jid";
    public static final String COLUMN_MEMBER_NICK_NAME = "nick_name";
    public static final String COLUMN_AFFILATION = "affilation";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_ROOM_JID = "room_jid";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER primary key autoincrement, "
            + COLUMN_MEMBER_JID + " TEXT not null, "
            + COLUMN_ROOM_JID + " TEXT not null,"
            + COLUMN_MEMBER_NICK_NAME + " TEXT,"
            + COLUMN_AFFILATION + " TEXT ,"
            + COLUMN_ROLE + " TEXT "
            + ");";

    public ChatRoomMemberTable() {
        super(TABLE_NAME);
    }

    @Override
    public void create(SQLiteDatabase database) throws SQLException {
        Log.d(ChatRoomMemberTable.class.getCanonicalName(), "creating Chatroom Members table");
        database.execSQL(SQL_CREATE);
    }

}
