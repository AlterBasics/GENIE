package abs.sf.client.genie.messaging;

import java.io.InputStream;

import abs.ixi.client.file.sfcm.ContentType;

/**
 * MediaContent captures a {@link ChatLine} with a media within a conversation.
 * Further it stores the exact type of the media that this MediaContent has
 * captured.
 */
public class MediaContent implements ChatLineContent {
	private long uuid;
	private String mediaId;
	private ContentType contentType;
	private InputStream mediaThumb;
	private String mediaPath;

	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public InputStream getMediaThumb() {
		return mediaThumb;
	}

	public void setMediaThumb(InputStream mediaThumb) {
		this.mediaThumb = mediaThumb;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}
}
