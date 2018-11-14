package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;


/**
 * Interface to map a database table row to an object instance.
 */
public interface RowMapper<T> {
    public T map(Cursor cursor) throws SQLException;
}