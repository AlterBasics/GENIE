package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserProfileTable extends DatabaseTable {
    public static final String TABLE_NAME = "user";

    public static final String COLUMN_ID = "uuid";
    public static final String COLUMN_JID = "jid";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_MIDDLE_NAME = "middle_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_NICK_NAME = "nick_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_BDAY = "bday";
    public static final String COLUMN_ADDRESS_HOME= "address_home";
    public static final String COLUMN_ADDRESS_STREET = "address_street";
    public static final String COLUMN_ADDRESS_LOCALITY = "address_locality";
    public static final String COLUMN_ADDRESS_CITY = "address_city";
    public static final String COLUMN_ADDRESS_STATE = "address_state";
    public static final String COLUMN_ADDRESS_COUNTRY = "address_country";
    public static final String COLUMN_ADDRESS_PCODE = "address_pcode";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_AVATAR_MEDIA_TYPE= "avatar_media_type";

    // Database creation SQL statement
    private static final String SQL_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER primary key autoincrement, "
            + COLUMN_JID + " TEXT not null, "
            + COLUMN_FIRST_NAME + " TEXT,"
            + COLUMN_MIDDLE_NAME + " TEXT, "
            + COLUMN_LAST_NAME + " TEXT, "
            + COLUMN_NICK_NAME + " TEXT, "
            + COLUMN_EMAIL + " TEXT, "
            + COLUMN_PHONE + " TEXT,"
            + COLUMN_GENDER + " TEXT, "
            + COLUMN_BDAY + " TEXT, "
            + COLUMN_ADDRESS_HOME + " TEXT,"
            + COLUMN_ADDRESS_STREET + " TEXT, "
            + COLUMN_ADDRESS_LOCALITY + " TEXT,"
            + COLUMN_ADDRESS_CITY + " TEXT, "
            + COLUMN_ADDRESS_STATE + " TEXT, "
            + COLUMN_ADDRESS_COUNTRY + " TEXT, "
            + COLUMN_ADDRESS_PCODE + " TEXT,"
            + COLUMN_AVATAR + " BLOB, "
            + COLUMN_AVATAR_MEDIA_TYPE + " TEXT"
            + ");";

    public UserProfileTable() {
        super(TABLE_NAME);
    }

    public void create(SQLiteDatabase database) throws SQLException {
        Log.d(RosterTable.class.getCanonicalName(), "creating user profile table");
        database.execSQL(SQL_CREATE);
    }
}
