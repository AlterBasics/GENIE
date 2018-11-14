package abs.sf.client.gini.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import abs.ixi.client.util.CollectionUtils;
import abs.sf.client.gini.db.exception.DataIntegrityException;
import abs.sf.client.gini.db.mapper.RowMapper;
import abs.sf.client.gini.db.object.ChatRoomMemberTable;
import abs.sf.client.gini.db.object.ChatStoreTable;
import abs.sf.client.gini.db.object.ChatArchiveTable;
import abs.sf.client.gini.db.object.ConversationTable;
import abs.sf.client.gini.db.object.DatabaseTable;
import abs.sf.client.gini.db.object.MediaStoreTable;
import abs.sf.client.gini.db.object.PollResponseTable;
import abs.sf.client.gini.db.object.RosterTable;
import abs.sf.client.gini.db.object.PresenceTable;
import abs.sf.client.gini.db.object.UndeliverStanzaTable;
import abs.sf.client.gini.db.object.UserProfileTable;

class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "stringflow.db";
    private static final int DATABASE_VERSION = 1;

    private static List<DatabaseTable> tables;

    static {

        tables = new ArrayList<>();

        tables.add(new ChatArchiveTable());
        tables.add(new ChatRoomMemberTable());
        tables.add(new ChatStoreTable());
        tables.add(new ConversationTable());
        tables.add(new MediaStoreTable());
        tables.add(new PollResponseTable());
        tables.add(new PresenceTable());
        tables.add(new RosterTable());
        tables.add(new UserProfileTable());
        tables.add(new UndeliverStanzaTable());
    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(DbHelper.class.getCanonicalName(), "Creating SQL tables");

        for (DatabaseTable table : tables) {
            try {
                table.create(database);
            } catch (Exception e) {
                Log.e(DbHelper.class.getCanonicalName(), "Failed to create table " + table.getName(), e);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        for (DatabaseTable table : tables) {
            table.drop(database);
            table.create(database);
        }
    }

    /**
     * inserts row in te given table
     *
     * @param tableName
     * @param values
     */
    public long insert(String tableName, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();

        long l = db.insert(tableName, null, values);

        return l;
    }


    /**
     * truncates the given table. Caution, you loose all the data along with table definition.
     *
     * @param tableName
     */
    public void truncateTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();

        // db.execSQL("TRUNCATE TABLE " + tableName);
        db.delete(tableName, null, null);
    }

    /**
     * truncates the all table. Caution, you loose all the data along with table definition.
     *
     * @param
     */
    public void truncateAllTables() {
        for (DatabaseTable table : tables) {
            truncateTable(table);
        }
    }

    public void truncateTable(DatabaseTable table) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(table.getName(), null, null);
        } catch (Exception e) {
            Log.e(DbHelper.class.getCanonicalName(), "Truncate all table" + table.getName(), e);
        }
    }

    /**
     * drops the given table. Caution, you loose all the data along with table definition.
     *
     * @param tableName
     */
    public void dropTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    /**
     * Update values in a table based on a where clause.
     *
     * @param tableName
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(tableName, values, whereClause, whereArgs);
    }

    /**
     * Convenience method to delete rows from a table based on a single where clause conditiion.
     *
     * @param tableName
     * @param columnName
     * @param value
     */
    public void delete(String tableName, String columnName, String value) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM  " + tableName + " WHERE " + columnName + " = '" + value + "'");
    }

    /**
     * Query an Integer value from database. The SQL is executed on the database
     * and returned value is mapped to an Integer. If the query returns a resultset
     * which can not be mapped to an Integer, the method throws {@link DataIntegrityException}
     *
     * @param sql
     * @return if data found return positive int value else return -1 while no data found for given query.
     * @throws DataIntegrityException
     */
    public long queryInt(final String sql) throws DataIntegrityException {
        return queryInt(sql, null);
    }

    /**
     * Query an Integer value from database. The SQL is executed on the database
     * and returned value is mapped to an Integer. If the query returns a resultset
     * which can not be mapped to an Integer, the method throws {@link DataIntegrityException}
     *
     * @param sql
     * @param selectionArgs
     * @return if data found return positive int value else return -1 while no data found for given query.
     */
    public int queryInt(final String sql, String[] selectionArgs) throws DataIntegrityException {
        List<Integer> l = query(sql, selectionArgs, new RowMapper<Integer>() {
            @Override
            public Integer map(Cursor cursor) throws DataIntegrityException {
                if (cursor.getCount() != 1) {
                    throw new DataIntegrityException("Inconsistent data for " + sql);
                }

                return cursor.getInt(0);
            }
        });

        if (!CollectionUtils.isNullOrEmpty(l)) {
            return l.get(0);

        } else {

            return -1;
        }
    }

    public long queryLong(final String sql, String[] selectionArgs) throws DataIntegrityException {
        List<Long> l = query(sql, selectionArgs, new RowMapper<Long>() {
            @Override
            public Long map(Cursor cursor) throws DataIntegrityException {
                if (cursor.getCount() != 1) {
                    throw new DataIntegrityException("Inconsistent data for " + sql);
                }
                return cursor.getLong(0);
            }
        });

        if (!CollectionUtils.isNullOrEmpty(l)) {
            return l.get(0);

        } else {
            return -1;
        }
    }

    /**
     * Query an String value from database. The SQL is executed on the database
     * and returned value is mapped to an String. If the query returns a resultset
     * which can not be mapped to an String, the method throws {@link DataIntegrityException}
     *
     * @param sql
     * @return
     * @throws DataIntegrityException
     */
    public String queryString(final String sql) throws DataIntegrityException {
        return queryString(sql, null);
    }

    /**
     * Query an String value from database. The SQL is executed on the database
     * and returned value is mapped to an String. If the query returns a resultset
     * which can not be mapped to an String, the method throws {@link DataIntegrityException}
     *
     * @param sql
     * @param selectionArgs
     * @return
     */
    public String queryString(final String sql, String[] selectionArgs) throws DataIntegrityException {
        List<String> l = query(sql, selectionArgs, new RowMapper<String>() {
            @Override
            public String map(Cursor cursor) throws DataIntegrityException {
                return cursor.getString(0);
            }
        });

        if (!CollectionUtils.isNullOrEmpty(l)) {
            return l.get(0);

        } else {
            return null;
        }
    }

    /**
     * Executes the query on database held by this helper. Selected rows
     * are mapped to objects using the {@link RowMapper} supplied. This
     * method does not accept query parameters.
     *
     * @param sql    sql query to be executed
     * @param mapper {@link RowMapper} instance to map database table row to object
     * @param <T>    type of the object to which a row will be mapped
     * @return list of objects; each object contains a row in table. Returns null
     * if there is no data found
     */
    public <T> List<T> query(String sql, RowMapper<T> mapper) {
        return query(sql, null, mapper);
    }

    /**
     * Executes the query on database held by this helper. Selected rows
     * are mapped to objects using the {@link RowMapper} supplied. Selection
     * Arguments are substituted using {@link SQLiteDatabase} native methods.
     * As dictated by {@link SQLiteDatabase}, the argument substitution will
     * be of String type.
     *
     * @param sql    sql query to be executed
     * @param mapper {@link RowMapper} instance to map database table row to object
     * @param <T>    type of the object to which a row will be mapped
     * @return list of objects; each object contains a row in table. Returns null
     * if there is no data found
     */
    public <T> List<T> query(String sql, String[] selectionArgs, RowMapper<T> mapper) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        if (cursor != null) {
            List<T> list = new ArrayList<>();

            while (cursor.moveToNext()) {
                list.add(mapper.map(cursor));
            }
            return list;
        }
        return null;
    }

    /**
     * Executes the query on database held by this helper. Selected rows
     * are mapped to objects using the {@link RowMapper} supplied. Selection
     * Arguments are substituted using {@link SQLiteDatabase} native methods.
     * As dictated by {@link SQLiteDatabase}, the argument substitution will
     * be of String type.
     *
     * @param sql    sql query to be executed
     * @param mapper {@link RowMapper} instance to map database table row to object
     * @param <T>    type of the object to which a row will be mapped
     * @return object; object contains a row in table. Returns null
     * if there is no data found
     */
    public <T> T queryForObject(String sql, String[] selectionArgs, RowMapper<T> mapper) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        if (cursor != null) {
            if (cursor.moveToNext()) {
                return mapper.map(cursor);
            }
        }

        return null;
    }
}