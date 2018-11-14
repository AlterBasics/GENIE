package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.util.ObjectUtils;
import abs.ixi.client.xmpp.packet.Stanza;

public class UndeliverStanzaRowMapper implements RowMapper<Stanza> {
    @Override
    public Stanza map(Cursor cursor) throws SQLException {
        byte[] bytes = cursor.getBlob(0);
        return (Stanza) ObjectUtils.deserialize(bytes);
    }

}
