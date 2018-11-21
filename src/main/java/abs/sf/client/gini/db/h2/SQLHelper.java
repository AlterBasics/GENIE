package abs.sf.client.gini.db.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import abs.ixi.client.util.CollectionUtils;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.db.mapper.RowMapper;

public class SQLHelper {
	private static final Logger LOGGER = Logger.getLogger(SQLHelper.class.getName());

	public static PreparedStatement createPreparedStatement(Connection con, String sql, Object... param)
			throws DbException {
		try {
			PreparedStatement ps = con.prepareStatement(sql);

			if (param != null) {
				for (int i = 0; i < param.length; i++) {
					ps.setObject((i + 1), param[i]);
				}
			}

			return ps;

		} catch (Exception e) {
			LOGGER.warning("Failed to create PrepareStatement for query : " + sql);
			throw new DbException("Failed to create PrepareStatement", e);
		}

	}

	public static PreparedStatement createPreparedStatement(Connection con, String sql, int autoGeneratedKey,
			Object... param) throws DbException {
		try {
			PreparedStatement ps = con.prepareStatement(sql, autoGeneratedKey);

			if (param != null) {
				for (int i = 0; i < param.length; i++) {
					ps.setObject((i + 1), param[i]);
				}
			}

			return ps;

		} catch (Exception e) {
			LOGGER.warning("Failed to create PrepareStatement for query : " + sql);
			throw new DbException("Failed to create PrepareStatement", e);
		}

	}

	/**
	 * Query an Integer value from database. The SQL is executed on the database
	 * and returned value is mapped to an Integer.
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return if data found return positive int value else return -1 while no
	 *         data found for given query.
	 * @throws DbException
	 */
	public static int queryInt(final Connection conn, final String sql) throws DbException {
		return queryInt(conn, sql, null);
	}

	/**
	 * Query an Integer value from database. The SQL is executed on the database
	 * and returned value is mapped to an Integer.
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return if data found return positive int value else return -1 while no
	 *         data found for given query.
	 * @throws DbException
	 */
	public static int queryInt(final Connection conn, final String sql, Object[] selectionArgs) throws DbException {
		List<Integer> l = query(conn, sql, selectionArgs, new RowMapper<Integer>() {
			@Override
			public Integer map(ResultSet rs) throws SQLException {
				return rs.getInt(1);
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
	 * and returned value is mapped to an String.
	 *
	 * @param sql
	 * @return
	 * @throws DbException
	 */
	public static String queryString(final Connection conn, final String sql) throws DbException {
		return queryString(conn, sql, null);
	}

	/**
	 * Query an String value from database. The SQL is executed on the database
	 * and returned value is mapped to an String.
	 *
	 * @param sql
	 * @param selectionArgs
	 * @return
	 * @throws DbException
	 */
	public static String queryString(final Connection conn, final String sql, Object[] selectionArgs)
			throws DbException {
		List<String> l = query(conn, sql, selectionArgs, new RowMapper<String>() {
			@Override
			public String map(ResultSet rs) throws SQLException {
				return rs.getString(1);
			}
		});

		if (!CollectionUtils.isNullOrEmpty(l)) {
			return l.get(0);

		} else {
			return null;
		}
	}

	/**
	 * Executes the query on database held by this helper. Selected rows are
	 * mapped to objects using the {@link RowMapper} supplied. This method does
	 * not accept query parameters.
	 *
	 * @param sql sql query to be executed
	 * @param mapper {@link RowMapper} instance to map database table row to
	 *            object
	 * @param <T> type of the object to which a row will be mapped
	 * @return list of objects; each object contains a row in table. Returns
	 *         null if there is no data found
	 * @throws DbException
	 */
	public static <T> List<T> query(final Connection conn, final String sql, final RowMapper<T> mapper)
			throws DbException {
		return query(conn, sql, null, mapper);
	}

	/**
	 * Executes the query on database. Selected rows are mapped to objects using
	 * the {@link RowMapper} supplied.
	 *
	 * @param sql sql query to be executed
	 * @param mapper {@link RowMapper} instance to map database table row to
	 *            object
	 * @param <T> type of the object to which a row will be mapped
	 * @return list of objects; each object contains a row in table. Returns
	 *         null if there is no data found
	 * @throws DbException
	 */
	public static <T> List<T> query(final Connection conn, final String sql, final Object[] selectionArgs,
			final RowMapper<T> mapper) throws DbException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, sql, selectionArgs);
			rs = ps.executeQuery();

			if (rs != null) {
				List<T> list = new ArrayList<>();

				while (rs.next()) {
					list.add(mapper.map(rs));
				}

				return list;
			}

		} catch (SQLException e) {
			LOGGER.warning("Failed to execute query : " + sql);
			throw new DbException("Failed to execute Query", e);

		} finally {
			closeResultSet(rs);
			closeStatement(ps);
		}

		return null;

	}

	/**
	 * Executes the query on database. Selected rows are mapped to objects using
	 * the {@link RowMapper} supplied.
	 *
	 * @param sql sql query to be executed
	 * @param mapper {@link RowMapper} instance to map database table row to
	 *            object
	 * @param <T> type of the object to which a row will be mapped
	 * @return object; object contains a row in table. Returns null if there is
	 *         no data found
	 */
	public static <T> T queryForObject(final Connection conn, final String sql, final Object[] selectionArgs,
			final RowMapper<T> mapper) throws DbException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = SQLHelper.createPreparedStatement(conn, sql, selectionArgs);
			rs = ps.executeQuery();

			if (rs != null && rs.next()) {
				return mapper.map(rs);
			}

		} catch (SQLException e) {
			LOGGER.warning("Failed to execute query : " + sql);
			throw new DbException("Failed to execute Query", e);

		} finally {
			closeResultSet(rs);
			closeStatement(ps);
		}

		return null;
	}

	/**
	 * Closes {@link Connection} obj.
	 * 
	 * @param conn
	 * @throws DbException
	 */
	public static void closeConnection(final Connection conn) throws DbException {
		LOGGER.fine("Closing database connection " + conn);

		if (conn != null) {
			try {

				conn.close();

			} catch (SQLException e) {
				LOGGER.warning("Failed to close database connection " + conn);
				throw new DbException("Failed to close database connection ", e);
			}
		}
	}

	/**
	 * Closes {@link Statement} obj.
	 * 
	 * @param conn
	 * @throws DbException
	 */
	public static void closeStatement(final Statement st) throws DbException {
		LOGGER.fine("Closing Staement " + st);

		if (st != null) {
			try {

				st.close();

			} catch (SQLException e) {
				LOGGER.warning("Failed to close Statement connection " + st);
				throw new DbException("Failed to close database s ", e);
			}
		}
	}

	/**
	 * Closes {@link ResultSet} obj.
	 * 
	 * @param conn
	 * @throws DbException
	 */
	public static void closeResultSet(final ResultSet rs) throws DbException {
		LOGGER.fine("Closing database ResultSet " + rs);

		if (rs != null) {
			try {

				rs.close();

			} catch (SQLException e) {
				LOGGER.warning("Failed to close database ResultSet " + rs);
				throw new DbException("Failed to close database ResultSet ", e);
			}
		}
	}

	public static String prepareLikeClause(String str, MatchType match) {
		switch (match) {
		case PREMATCH:
			return '%' + str;
		case POSTMATCH:
			return str + '%';
		case BOTH:
			return '%' + str + '%';
		}

		return '%' + str + '%';
	}

	public enum MatchType {
		PREMATCH, POSTMATCH, BOTH
	}
}
