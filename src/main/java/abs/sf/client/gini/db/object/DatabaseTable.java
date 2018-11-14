package abs.sf.client.gini.db.object;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import abs.sf.client.android.db.exception.DbException;

/**
 * Represents a database table in SqlLite
 */
public abstract class DatabaseTable {
    protected String tableName;

    public DatabaseTable(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Returns this table name
     *
     * @return
     */
    public String getName() {
        return this.tableName;
    }

    /**
     * Creates this table definition in the database
     *
     * @param database
     * @throws DbException
     */
    public abstract void create(SQLiteDatabase database) throws SQLException;

    /**
     * drops this table from database
     *
     * @param database
     * @throws DbException
     */
    public void drop(SQLiteDatabase database) throws SQLException {
        String sql = "drop table " + this.tableName + ";";
        database.execSQL(sql);
    }


    /**
     * Truncates all the data from this table
     *
     * @param database
     * @throws DbException
     */
    public void truncate(SQLiteDatabase database) throws SQLException {
        String sql = "truncate table " + this.tableName + ";";
        database.execSQL(sql);
    }

}
