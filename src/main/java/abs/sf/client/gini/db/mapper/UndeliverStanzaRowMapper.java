package abs.sf.client.gini.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import abs.ixi.client.xmpp.packet.Stanza;

public class UndeliverStanzaRowMapper implements RowMapper<Stanza> {
	@Override
	public Stanza map(ResultSet rs) throws SQLException {
		// return (Stanza)
		// ObjectUtils.deserialize(rs.getBlob(1).getBinaryStream());
		return null;
	}

}
