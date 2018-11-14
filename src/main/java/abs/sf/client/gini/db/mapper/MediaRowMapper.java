package abs.sf.client.gini.db.mapper;

import android.database.Cursor;
import android.database.SQLException;

import abs.ixi.client.file.sfcm.ContentType;
import abs.sf.client.gini.messaging.MediaContent;

public class MediaRowMapper implements RowMapper<MediaContent>{

    @Override
    public MediaContent map(Cursor cursor) throws SQLException {
        MediaContent media = new MediaContent();
        media.setUuid(cursor.getLong(0));
        media.setMediaId(cursor.getString(1));
        media.setMediaThumb(cursor.getBlob(2));
        media.setMediaPath(cursor.getString(3));
        media.setContentType(new ContentType(cursor.getString(4)));

        return media;
    }

}
