package abs.sf.client.gini.messaging;

import java.io.Serializable;

import abs.ixi.client.util.UUIDGenerator;
import abs.ixi.client.xmpp.JID;

public class ChatLine implements Serializable {
	private static final long serialVersionUID = 1L;

	private String conversationId;
	private String messageId;
	private String peerName;
	private String peerBareJid;
	private String peerResource;
	private Direction direction;
	private String text;
	private long contentId;
	private ContentType contentType;
	private String displayTime;
	private long createTime;
	private MessageStatus messageStatus;
	private boolean mdrRequested;
	private boolean markable;
	private boolean haveSean;
	private boolean csnActive;

	public ChatLine() {
	}

	public ChatLine(String messageId, String peerBareJid, Direction direction) {
		this(UUIDGenerator.secureId(), messageId, peerBareJid, direction);
	}

	public ChatLine(String conversationId, String messageId, JID peerJID, Direction direction) {
		this(conversationId, messageId, peerJID.getBareJID(), direction);
	}

	public ChatLine(String conversationId, String messageId, String peerBareJid, Direction direction) {
		this.conversationId = conversationId;
		this.messageId = messageId;
		this.peerBareJid = peerBareJid;
		this.direction = direction;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getPeerName() {
		return peerName;
	}

	public void setPeerName(String peerName) {
		this.peerName = peerName;
	}

	public String getPeerBareJid() {
		return peerBareJid;
	}

	public void setPeerBareJid(String peerBareJid) {
		this.peerBareJid = peerBareJid;
	}

	public String getPeerResource() {
		return peerResource;
	}

	public void setPeerResource(String peerResource) {
		this.peerResource = peerResource;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set content type from a String. The method parses the String to infer
	 * content type
	 *
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		try {
			this.contentType = ContentType.valueOf(contentType);
		} catch (Exception e) {
			this.contentType = ContentType.TEXT;
		}
	}

	public String getDisplayTime() {
		return displayTime;
	}

	public void setDisplayTime(String displayTime) {
		this.displayTime = displayTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getContentId() {
		return contentId;
	}

	public void setContentId(long contentId) {
		this.contentId = contentId;
	}

	public MessageStatus getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(MessageStatus messageStatus) {
		this.messageStatus = messageStatus;
	}

	public boolean isMarkable() {
		return markable;
	}

	public void setMarkable(boolean markable) {
		this.markable = markable;
	}

	public boolean isHaveSean() {
		return haveSean;
	}

	public boolean isMdrRequested() {
		return mdrRequested;
	}

	public void setMdrRequested(boolean mdrRequested) {
		this.mdrRequested = mdrRequested;
	}

	public boolean haveSean() {
		return haveSean;
	}

	public void setHaveSean(boolean haveSean) {
		this.haveSean = haveSean;
	}

	public boolean isCsnActive() {
		return csnActive;
	}

	public void setCsnActive(boolean csnActive) {
		this.csnActive = csnActive;
	}

	/**
	 * The enum represents the content type of the chatline
	 */
	public enum ContentType {
		TEXT, MEDIA;
	}

	/**
	 * {@link Direction} represents the flow direction of the chatline
	 */
	public enum Direction {
		SEND(0), RECEIVE(1);

		private int val;

		Direction(int val) {
			this.val = val;
		}

		public int val() {
			return this.val;
		}

		public static Direction valueFrom(int val) {
			for (Direction d : values()) {
				if (d.val == val) {
					return d;
				}
			}

			throw new IllegalArgumentException("Supplied value for direction is not valid");
		}
	}

	public enum MessageStatus {
		NOT_DELIVERED_TO_SERVER(0), DELIVERED_TO_SERVER(1), DELIVERED_TO_RECEIVER(2), RECEIVER_IS_ACKNOWLEDGED(
				3), RECEIVER_HAS_VIEWED(4), INCOMMING_MESSAGE(-1);

		public int value;

		private MessageStatus(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static MessageStatus statusFrom(int val) throws IllegalArgumentException {
			for (MessageStatus status : values()) {
				if (status.getValue() == val) {
					return status;
				}
			}
			throw new IllegalArgumentException("No MsgType for value [" + val + "]");
		}

	}

}
