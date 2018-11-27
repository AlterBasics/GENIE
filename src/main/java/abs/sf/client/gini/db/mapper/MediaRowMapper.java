package abs.sf.client.gini.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import abs.ixi.client.file.sfcm.ContentType;
import abs.sf.client.gini.messaging.MediaContent;

public class MediaRowMapper implements RowMapper<MediaContent>{

    @Override
    public MediaContent map(ResultSet rs) throws SQLException {
        MediaContent media = new MediaContent();
        media.setUuid(rs.getLong(0));
        media.setMediaId(rs.getString(1));
        media.setMediaThumb(rs.getBlob(2));
        media.setMediaPath(rs.getString(3));
        media.setContentType(new ContentType(rs.getString(4)));

        return media;
    }

}
