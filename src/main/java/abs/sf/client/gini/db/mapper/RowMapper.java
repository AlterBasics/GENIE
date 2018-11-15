package abs.sf.client.gini.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Interface to map a database table row to an object instance.
 */
public interface RowMapper<T> {
    public T map(ResultSet rs) throws SQLException;
}