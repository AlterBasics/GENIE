package abs.sf.client.gini.db.object;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Represents a database table in h2
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
     * @param conn
     * @throws SQLException
     */
    public abstract void create(Connection conn) throws SQLException;

    /**
     * drops this table from database
     *
     * @param conn
     * @throws SQLException
     */
    public void drop(Connection conn) throws SQLException {
        String sql = "drop table if exists " + this.tableName + ";";
        Statement statement = conn.createStatement();
    	statement.executeUpdate(sql);
    }


    /**
     * Truncates all the data from this table
     *
     * @param conn
     * @throws SQLException
     */
    public void truncate(Connection conn) throws SQLException {
        String sql = "truncate table " + this.tableName + ";";
        Statement statement = conn.createStatement();
    	statement.executeUpdate(sql);
    }

}
