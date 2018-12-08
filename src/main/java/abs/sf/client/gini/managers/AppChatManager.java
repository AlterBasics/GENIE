package abs.sf.client.gini.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.ChatManager;
import abs.ixi.client.core.Callback;
import abs.ixi.client.core.Packet;
import abs.ixi.client.core.event.Event;
import abs.ixi.client.core.event.EventHandler;
import abs.ixi.client.file.sfcm.ContentType;
import abs.ixi.client.file.sfcm.FileTransfer;
import abs.ixi.client.io.XMPPStreamManager;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.util.UUIDGenerator;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.CMAcknowledged;
import abs.ixi.client.xmpp.packet.CMDisplayed;
import abs.ixi.client.xmpp.packet.CMReceived;
import abs.ixi.client.xmpp.packet.MDRReceived;
import abs.ixi.client.xmpp.packet.Message;
import abs.ixi.client.xmpp.packet.MessageBody;
import abs.ixi.client.xmpp.packet.MessageContent;
import abs.ixi.client.xmpp.packet.MessageDelay;
import abs.ixi.client.xmpp.packet.MessageMedia;
import abs.ixi.client.xmpp.packet.MessageSubject;
import abs.ixi.client.xmpp.packet.MessageThread;
import abs.sf.client.gini.db.DbManager;
import abs.sf.client.gini.db.exception.DbException;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.messaging.ChatLine;
import abs.sf.client.gini.messaging.ChatListener;
import abs.sf.client.gini.messaging.Conversation;
import abs.sf.client.gini.utils.SFSDKProperties;

public class AppChatManager extends ChatManager {
	private static final Logger LOGGER = Logger.getLogger(AppChatManager.class.getName());

	private static final String MEDIA_RECEIVED_TEXT = "You received a media message";
	private static final String MEDIA_SENT_TEXT = "You sent a media message";

	private List<ChatListener> chatListeners;
	private boolean isChatMarkersEnabled;
	private boolean isChatStateNotificationEnabled;
	private boolean isMessageDeliveryReceiptEnabled;

	public AppChatManager(XMPPStreamManager streamManager) throws StringflowErrorException {
		super(streamManager);
		this.chatListeners = Collections.synchronizedList(new ArrayList<ChatListener>());
		this.isChatMarkersEnabled = SFSDKProperties.getInstance().isChatMarkersEnabled();
		this.isChatStateNotificationEnabled = SFSDKProperties.getInstance().isChatStateNotificationEnabled();
		this.isMessageDeliveryReceiptEnabled = SFSDKProperties.getInstance().isMessageDeliveryReceiptEnabled();
	}

	/**
	 * Add ChatLine listener to receiver incoming chatlines on UI(Activities)
	 *
	 * @param chatListener
	 */
	public void addChatListener(ChatListener chatListener) {
		this.chatListeners.add(chatListener);
	}

	/**
	 * Remove ChatLine listener.
	 *
	 * @param chatListener
	 */
	public void removeChatListener(ChatListener chatListener) {
		this.chatListeners.remove(chatListener);
	}

	public void setChatMarkersEnabled(boolean chatMarkersEnabled) {
		isChatMarkersEnabled = chatMarkersEnabled;
	}

	public void setChatStateNotificationEnabled(boolean chatStateNotificationEnabled) {
		isChatStateNotificationEnabled = chatStateNotificationEnabled;
	}

	public void setMessageDeliveryReceiptEnabled(boolean messageDeliveryReceiptEnabled) {
		isMessageDeliveryReceiptEnabled = messageDeliveryReceiptEnabled;
	}

	/**
	 * To send Text message
	 *
	 * @param msg
	 * @param toJID
	 * @param isGroup
	 * @return
	 * @throws StringflowErrorException
	 * @throws InvalidJabberId
	 */
	public ChatLine sendTextMessage(String msg, JID toJID, boolean isGroup) throws StringflowErrorException {
		return this.sendTextMessage(UUIDGenerator.secureId(), msg, toJID, isGroup);
	}

	/**
	 * To send Text Message
	 *
	 * @param conversationId
	 * @param msg
	 * @param toJID
	 * @param isGroup
	 * @return
	 * @throws StringflowErrorException
	 */
	public ChatLine sendTextMessage(String conversationId, String msg, JID toJID, boolean isGroup)
			throws StringflowErrorException {
		ChatLine chatLine = new ChatLine(conversationId, UUIDGenerator.secureId(), toJID, ChatLine.Direction.SEND);

		chatLine.setContentType(ChatLine.ContentType.TEXT);
		chatLine.setText(msg);

		chatLine.setCreateTime(DateUtils.currentTimeInMiles());
		chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

		chatLine.setMessageStatus(ChatLine.MessageStatus.NOT_DELIVERED_TO_SERVER);

		this.storeChatLine(chatLine);

		this.sendTextMessage(conversationId, chatLine.getMessageId(), chatLine.getText(), toJID, isGroup,
				isChatMarkersEnabled, isMessageDeliveryReceiptEnabled, isChatStateNotificationEnabled);

		return chatLine;
	}

	/**
	 * Use this method to send new media Message. This will send {@link Message}
	 * packet to receiver and also trigger media file upload.
	 * <p>
	 * For retry to upload media file user
	 * {@link #reSendMedia(String, File, ContentType, JID, Callback)}.
	 * </p>
	 *
	 * @param mediaFile {@link File} object of media which have to send
	 * @param mediaThumb thumbnail of media file
	 * @param contentType {@link ContentType} of media
	 * @param toJID media receiver {@link JID}
	 * @param isGroup Is media receiver is group or not
	 * @param callback {@link Callback} to know media transmission status
	 * @return
	 * @throws StringflowErrorException
	 */
	public ChatLine sendMediaMessage(File mediaFile, byte[] mediaThumb, ContentType contentType, JID toJID,
			boolean isGroup, Callback<String, FileTransfer.FailureReason> callback) throws StringflowErrorException {

		return this.sendMediaMessage(UUIDGenerator.secureId(), mediaFile, mediaThumb, contentType, toJID, isGroup,
				callback);
	}

	/**
	 * Use this method to send new media Message. This will send {@link Message}
	 * packet to receiver and also trigger media file upload.
	 * <p>
	 * <p>
	 * For retry to upload media file user
	 * {@link #reSendMedia(String, File, ContentType, JID, Callback)}.
	 * </p>
	 *
	 * @param conversationId conversation Id
	 * @param mediaFile {@link File} object of media which have to send
	 * @param mediaThumb thumbnail of media file
	 * @param contentType {@link ContentType} of media
	 * @param toJID media receiver {@link JID}
	 * @param isGroup Is media receiver is group or not
	 * @param callback {@link Callback} to know media transmission status
	 * @return
	 * @throws StringflowErrorException
	 */
	public ChatLine sendMediaMessage(String conversationId, File mediaFile, byte[] mediaThumb, ContentType contentType,
			JID toJID, boolean isGroup, Callback<String, FileTransfer.FailureReason> callback)
			throws StringflowErrorException {
		String mediaMessageId = UUIDGenerator.secureId();
		ChatLine chatLine = new ChatLine(conversationId, mediaMessageId, toJID, ChatLine.Direction.SEND);

		chatLine.setText(MEDIA_SENT_TEXT);

		chatLine.setContentType(ChatLine.ContentType.MEDIA);
		chatLine.setContentId(storeMessageMediaContent(mediaMessageId, mediaThumb, mediaFile, contentType));

		chatLine.setCreateTime(DateUtils.currentTimeInMiles());
		chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

		chatLine.setMessageStatus(ChatLine.MessageStatus.NOT_DELIVERED_TO_SERVER);

		this.storeChatLine(chatLine);

		this.sendMediaMessage(conversationId, mediaMessageId, contentType,
				Base64.getEncoder().encodeToString(mediaThumb), toJID, isGroup, isChatMarkersEnabled,
				isMessageDeliveryReceiptEnabled, isChatStateNotificationEnabled);

		this.uploadFile(mediaMessageId, mediaFile, contentType, toJID, callback);

		return chatLine;
	}

	/**
	 * Use this method to retry to upload media. It will only trigger media file
	 * transmission.
	 * <p>
	 * For New Media message use
	 * {@link #sendMediaMessage(String, File, byte[], ContentType, JID, boolean, Callback)}.
	 * </p>
	 *
	 * @param mediaId
	 * @param file
	 * @param contentType
	 * @param to
	 * @param callback
	 */
	public void reSendMedia(final String mediaId, final File file, ContentType contentType, final JID to,
			final Callback<String, FileTransfer.FailureReason> callback) {

		this.uploadFile(mediaId, file, contentType, to, callback);
	}

	@Override
	public void downloadFile(JID from, final String mediaId, final ContentType contentType,
			final Callback<File, FileTransfer.FailureReason> callback) {
		LOGGER.info("Downloading media for mediaId");
		this.downloadFile(from, mediaId, contentType, new Callback<File, FileTransfer.FailureReason>() {
			@Override
			public void onSuccess(File file) {
				try {

					DbManager.getInstance().updateMediaPath(mediaId, file.getAbsolutePath());

				} catch (DbException e) {
					LOGGER.log(Level.WARNING,
							"Failed to update media Path for mediaId : " + mediaId + " and file : " + file, e);
				}
				callback.onSuccess(file);
			}

			@Override
			public void onFailure(FileTransfer.FailureReason failureReason) {
				callback.onFailure(failureReason);
			}
		});
	}

	private long storeMessageMediaContent(String mediaId, byte[] mediaThumb, File mediaFile, ContentType contentType)
			throws StringflowErrorException {
		try {

			return DbManager.getInstance().storeMedia(mediaId, mediaThumb, mediaFile.getAbsolutePath(),
					contentType.getMimeType());

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to store message media content with mediaId : " + mediaId, e);
			throw new StringflowErrorException("Failed to store message media content due to " + e.getMessage(), e);
		}
	}

	private void storeChatLine(ChatLine chatLine) throws StringflowErrorException {
		try {
			DbManager.getInstance().addToChatStore(chatLine);
			DbManager.getInstance().addOrUpdateConversation(chatLine);

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to store chatLine with messageId : " + chatLine.getMessageId()
					+ " for pearJID : " + chatLine.getPeerBareJid(), e);
			throw new StringflowErrorException("Failed to store chatLine due to " + e.getMessage(), e);
		}

	}

	/**
	 * This method is called when user has viewed all unSean messages.
	 * 
	 * @throws StringflowErrorException
	 */
	public void sendAllUnReadMessageReadReceipt(JID contactJID) throws StringflowErrorException {
		List<ChatLine> unReadChatLines = null;
		try {
			unReadChatLines = DbManager.getInstance().getAllUnreadChatLines(contactJID.getBareJID());

		} catch (DbException e) {
			LOGGER.log(Level.WARNING,
					"Failed to fetch unread Chatline for sending read Receipt for contactJID : " + contactJID, e);
			throw new StringflowErrorException("Failed to fetch unread Chatline for sending read Receipt", e);
		}

		if (!CollectionUtils.isNullOrEmpty(unReadChatLines)) {

			for (ChatLine chatLine : unReadChatLines) {
				this.sendMessageReadReceipt(chatLine);
			}
		}
	}

	/**
	 * This method should be called when user has viewed given chatline to let
	 * the sender know that receiver has viewed his message.
	 *
	 * @param chatLine
	 * @throws StringflowErrorException
	 */
	public void sendMessageReadReceipt(ChatLine chatLine) throws StringflowErrorException {
		try {
			if (chatLine.isMarkable() && !chatLine.haveSean()) {
				this.sendMsgCMDisplayedReceipt(chatLine.getMessageId(), new JID(chatLine.getPeerBareJid()),
						DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));

				DbManager.getInstance().markMessageViewed(chatLine.getMessageId());
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to send message read receipt for messageId : " + chatLine.getMessageId(),
					e);
			throw new StringflowErrorException("Failed To send Message Read Receipt due to" + e.getMessage(), e);
		}
	}

	/**
	 * This method should be called at the time of notification generation for
	 * given {@link ChatLine} to let the sender know that receiver is
	 * acknowledged for his message.
	 *
	 * @param chatLine
	 * @throws StringflowErrorException
	 */
	public void sendMessageAcknowledgementReceipt(ChatLine chatLine) throws StringflowErrorException {
		try {
			if (chatLine.isMarkable()) {
				this.sendMsgCMAcknowledgedReceipt(chatLine.getMessageId(), new JID(chatLine.getPeerBareJid()),
						DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
					"Failed to send acknowledgement receipt for messageId : " + chatLine.getMessageId(), e);
			throw new StringflowErrorException("Failed to send acknowledgement receipt due to " + e.getMessage(), e);

		}
	}

	/**
	 * This method is called when new message is received to let the sender know
	 * that receiver is acknowledged for his message.
	 *
	 * @param chatLine
	 * @throws StringflowErrorException
	 */
	private void sendMessageReceivedReceipt(ChatLine chatLine) {
		try {
			if (chatLine.isMarkable()) {
				this.sendMsgCMReceivedReceipt(chatLine.getMessageId(), new JID(chatLine.getPeerBareJid()),
						DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));
			}

			if (chatLine.isMdrRequested()) {
				this.sendMsgMDRReceivedReceipt(chatLine.getMessageId(), new JID(chatLine.getPeerBareJid()),
						DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));
			}

		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
					"Failed to send message received receipt for messageId : " + chatLine.getMessageId(), e);
		}
	}

	/**
	 * When user has started typing a message for contact, this method should be
	 * called to tell contact about it.
	 *
	 * @param contactJID
	 */
	public void sendMessageTypingStarted(JID contactJID) {
		this.sendComposingCSN(contactJID);
	}

	/**
	 * When user has paused typing a message for contact, this method should be
	 * called to tell contact about it.
	 *
	 * @param contactJID
	 */
	public void sendMessageTypingStopped(JID contactJID) {
		this.sendPausedCSN(contactJID);
	}

	/**
	 * When user is not active with contact chat from a shorter time period (eg.
	 * since 2 minutes). this method should be called to tell contact about it.
	 *
	 * @param contactJID
	 */
	public void sendChatInactivity(JID contactJID) {
		this.sendInactiveCSN(contactJID);
	}

	/**
	 * When user is not active with contact chat from a longer time period (eg.
	 * since 10 minutes). this method should be called to tell contact about it.
	 *
	 * @param contactJID
	 */
	public void sendChattingStopped(JID contactJID) {
		this.sendGoneCSN(contactJID);
	}

	/**
	 * After reading all messages from contact. then mark no unread conversation
	 * available for that contact.
	 *
	 * @param contactJID
	 * @throws StringflowErrorException
	 */
	public void markNoUnreadConversation(JID contactJID) throws StringflowErrorException {
		try {
			DbManager.getInstance().updateUnreadConversationCount(contactJID.getBareJID(), 0);
		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to update Unread Conversation Count", e);
			throw new StringflowErrorException(
					"Failed to update Unread Conversation Count due to database operation failure", e);
		}
	}

	/**
	 * @param contactJID
	 * @param isGroup
	 * @return all conversations for given contact.
	 * @throws StringflowErrorException
	 */
	public List<ChatLine> getAllConversationChatLines(JID contactJID) throws StringflowErrorException {
		try {
			List<ChatLine> allChatLines = DbManager.getInstance().fetchConversationChatlines(contactJID.getBareJID());
			// TODO: Is it need to set pearName I think when we r persisting
			// chatline at that time we detrmine pearname and store it in db .
			// For dynamicly change of pear name we should determine pear name
			// not
			// at time of storing. Think about it.

			return allChatLines;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to get All Conversation Chatlines for contactJID " + contactJID, e);
			throw new StringflowErrorException("Failed to get All Conversation Chatlines due to db operation failure",
					e);
		}

	}

	/**
	 * @return All {@link Conversation} list for logged in user.
	 * @throws StringflowErrorException
	 */
	public List<Conversation> getAllConversations() throws StringflowErrorException {
		try {
			return DbManager.getInstance().fetchConversations();
		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to get All Conversation ", e);
			throw new StringflowErrorException("Failed to get All Conversation due to db operation failure", e);
		}
	}

	@Override
	public void collect(final Packet packet) {
		forwardPacket(packet);

		if (packet instanceof Message) {
			Message msg = (Message) packet;

			ChatLine chatLine;

			try {

				chatLine = prepareIncomingChatLine(msg);

			} catch (StringflowErrorException e1) {
				return;
			}

			if (!CollectionUtils.isNullOrEmpty(msg.getContents())) {

				for (MessageContent content : msg.getContents()) {

					if (content.isContentType(MessageContent.MessageContentType.BODY)) {
						MessageBody body = (MessageBody) content;
						chatLine.setContentType(ChatLine.ContentType.TEXT);
						chatLine.setText(body.getContent());

					} else if (content.isContentType(MessageContent.MessageContentType.THREAD)) {
						MessageThread thread = (MessageThread) content;
						chatLine.setConversationId(thread.getThreadId());

					} else if (content.isContentType(MessageContent.MessageContentType.MEDIA)) {
						MessageMedia media = (MessageMedia) content;
						try {
							long mediaId = storeMessageMediaContent(media);
							chatLine.setContentType(ChatLine.ContentType.MEDIA);
							chatLine.setContentId(mediaId);
							chatLine.setText(MEDIA_RECEIVED_TEXT);

						} catch (StringflowErrorException e) {
							return;
						}

					} else if (content.isContentType(MessageContent.MessageContentType.SUBJECT)) {
						MessageSubject subject = (MessageSubject) content;

						if (msg.getType() == Message.MessageType.GROUP_CHAT
								&& !StringUtils.isNullOrEmpty(subject.getSubject())) {
							changeChatRoomSubject(msg.getFrom(), subject.getSubject());
							return;
						}

					} else if (content.isContentType(MessageContent.MessageContentType.MDR_REQUEST)) {
						chatLine.setMdrRequested(true);

					} else if (content.isContentType(MessageContent.MessageContentType.CM_MARKABLE)) {
						chatLine.setMarkable(true);

					} else if (content.isContentType(MessageContent.MessageContentType.DELAY)) {
						MessageDelay delay = (MessageDelay) content;

						try {
							Long timeInMilles = DateUtils.getDateTime(delay.getStamp(), MessageDelay.TIME_FORMAT,
									TimeZone.getTimeZone(MessageDelay.UTC_TIMEZONE)).getTime();
							chatLine.setCreateTime(timeInMilles);
							chatLine.setDisplayTime(DateUtils.displayTime(timeInMilles));
						} catch (Exception e) {
							// Swallow exception
						}

					} else if (content.isContentType(MessageContent.MessageContentType.MDR_RECEIVED)) {
						MDRReceived received = (MDRReceived) content;

						try {
							DbManager.getInstance().markAsReceived(received.getMessageId());

							for (ChatListener listener : this.chatListeners) {
								listener.onMessageDeliveredToReceiver(received.getMessageId(), msg.getFrom());
							}

						} catch (DbException e) {
							LOGGER.log(Level.WARNING, "Failed to mark MDR Received for message : "
									+ received.getMessageId() + " from contact JID : " + msg.getFrom(), e);
							e.printStackTrace();
						}

						return;

					} else if (content.isContentType(MessageContent.MessageContentType.CM_RECEIVED)) {
						CMReceived received = (CMReceived) content;
						try {
							DbManager.getInstance().markAsReceived(received.getMessageId());

							for (ChatListener listener : this.chatListeners) {
								listener.onMessageDeliveredToReceiver(received.getMessageId(), msg.getFrom());
							}
						} catch (DbException e) {
							LOGGER.log(Level.WARNING, "Failed to mark CM Received for message : "
									+ received.getMessageId() + " from contact JID : " + msg.getFrom(), e);
							e.printStackTrace();
						}

						return;

					} else if (content.isContentType(MessageContent.MessageContentType.CM_ACKNOWLEDGED)) {
						CMAcknowledged acknowledged = (CMAcknowledged) content;
						try {
							DbManager.getInstance().markAsAcknowledged(acknowledged.getMessageId());

							for (ChatListener listener : this.chatListeners) {
								listener.onMessageAcknowledgedToReceiver(acknowledged.getMessageId(), msg.getFrom());
							}
						} catch (DbException e) {
							LOGGER.log(Level.WARNING, "Failed to mark CM Acknowledged for message : "
									+ acknowledged.getMessageId() + " from contact JID : " + msg.getFrom(), e);
							e.printStackTrace();
						}

						return;

					} else if (content.isContentType(MessageContent.MessageContentType.CM_DISPLAYED)) {
						CMDisplayed displayed = (CMDisplayed) content;
						try {
							DbManager.getInstance().markAsDisplayed(displayed.getMessageId(),
									chatLine.getPeerBareJid());

							for (ChatListener listener : this.chatListeners) {
								listener.onMessageViewedByReceiver(displayed.getMessageId(), msg.getFrom());
							}

						} catch (DbException e) {
							LOGGER.log(Level.WARNING, "Failed to mark MDR displayed for message : "
									+ displayed.getMessageId() + " from contact JID : " + msg.getFrom(), e);
							e.printStackTrace();
						}
						return;

					} else if (content.isContentType(MessageContent.MessageContentType.CSN_ACTIVE)) {
						chatLine.setCsnActive(true);

					} else if (content.isContentType(MessageContent.MessageContentType.CSN_COMPOSING)) {
						for (ChatListener listener : this.chatListeners) {
							listener.onContactTypingStarted(msg.getFrom());
						}

						return;

					} else if (content.isContentType(MessageContent.MessageContentType.CSN_PAUSED)) {
						for (ChatListener listener : this.chatListeners) {
							listener.onContactTypingPaused(msg.getFrom());
						}

						return;

					} else if (content.isContentType(MessageContent.MessageContentType.CSN_INACTIVE)) {
						for (ChatListener listener : this.chatListeners) {
							listener.onContactInactivityInUserChat(msg.getFrom());
						}

						return;

					} else if (content.isContentType(MessageContent.MessageContentType.CSN_GONE)) {
						for (ChatListener listener : this.chatListeners) {
							listener.onContactGoneFromUserChat(msg.getFrom());
						}

						return;

					}
				}

			}

			if (chatLine.getContentType() == ChatLine.ContentType.TEXT
					|| chatLine.getContentType() == ChatLine.ContentType.MEDIA) {

				try {
					boolean isMessageAlreadyExsist = DbManager.getInstance()
							.isMessageAlreadyExist(chatLine.getPeerBareJid(), chatLine.getMessageId());

					if (!isMessageAlreadyExsist) {
						storeChatLine(chatLine);

						sendMessageReceivedReceipt(chatLine);

						// generateLocalBroadcast(chatLine);

						notifyOnChatLine(chatLine);
					}

				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Failed to store recevied message with messageID : " + msg.getId()
							+ " from pearJID : " + msg.getFrom(), e);
				}
			}

		}
	}

	private long storeMessageMediaContent(MessageMedia media) throws StringflowErrorException {
		try {

			return DbManager.getInstance().storeMedia(media.getMediaId(), Base64.getDecoder().decode(media.getThumb()),
					null, media.getContentType().getMimeType());

		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to Store Message Media Content for mediaId : " + media.getMediaId(), e);
			throw new StringflowErrorException(
					"Failed to Store Message Media Content due to database operation failure", e);
		}
	}

	private void changeChatRoomSubject(JID roomJID, String subject) {
		try {
			DbManager.getInstance().updateChatRoomSubject(roomJID.getBareJID(), subject);
			// TODO: may send subject change to listener to reflect on UI at run
			// time.
		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to Update ChatRoom subhject for roomJID : " + roomJID, e);
		}
	}

	private ChatLine prepareIncomingChatLine(Message msg) throws StringflowErrorException {
		try {
			String from = msg.getFrom().getBareJID();

			ChatLine chatLine = new ChatLine(msg.getId(), from, ChatLine.Direction.RECEIVE);
			chatLine.setMessageStatus(ChatLine.MessageStatus.INCOMMING_MESSAGE);

			if (msg.getFrom().getResource() != null) {
				chatLine.setPeerResource(msg.getFrom().getResource().trim());
			}
			chatLine.setCreateTime(DateUtils.currentTimeInMiles());
			chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

			if (msg.getType() == Message.MessageType.GROUP_CHAT) {
				String memberNickName = chatLine.getPeerResource();
				String memberJID = DbManager.getInstance().getChatRoomMemberJID(msg.getFrom().getBareJID(),
						memberNickName);

				String userName = DbManager.getInstance().getRosterItemName(memberJID);

				if (StringUtils.isNullOrEmpty(userName)) {
					chatLine.setPeerName(memberNickName);

				} else {
					chatLine.setPeerName(userName);
				}

			} else {
				String userName = DbManager.getInstance().getRosterItemName(msg.getFrom().getBareJID());

				if (StringUtils.isNullOrEmpty(userName)) {
					chatLine.setPeerName(msg.getFrom().getNode());

				} else {
					chatLine.setPeerName(userName);
				}
			}

			return chatLine;
		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to prepare chatline for incomming message with messageId : " + msg.getId()
					+ " from pearJID : " + msg.getFrom() + " Due to Database operation failure  ", e);
			throw new StringflowErrorException("Failed to prepare chatline for incomming message with messageId : "
					+ msg.getId() + " from pearJID : " + msg.getFrom() + " Due to Database operation failure  ", e);
		}
	}

	private void notifyOnChatLine(ChatLine chatLine) {
		if (!CollectionUtils.isNullOrEmpty(this.chatListeners)) {
			for (ChatListener listener : this.chatListeners) {
				listener.onNewMessageReceived(chatLine);
			}
		}
	}

	// private static void generateLocalBroadcast(final ChatLine chatLine) {
	// ContextProvider contextProvider = (ContextProvider)
	// Platform.getInstance().getSession()
	// .get(ContextProvider.KEY_CONTEXT);
	// final Context context = contextProvider.context();
	// Intent intent = new Intent(SFConstants.ACTION_ON_MESSAGE);
	// intent.putExtra(SFConstants.CHATLINE_OBJECT, chatLine);
	// context.sendBroadcast(intent);
	// }

	public class MessageAckHandler implements EventHandler {
		@Override
		public void handle(Event event) {
			@SuppressWarnings("unchecked")
			List<Message> messages = (List<Message>) event.getSource();

			if (!CollectionUtils.isNullOrEmpty(messages)) {

				for (Message message : messages) {
					try {
						if (!DbManager.getInstance().isMessageAlreadyDelivered(message.getId())) {

							DbManager.getInstance().updateDeliveryStatus(message.getId(),
									ChatLine.MessageStatus.DELIVERED_TO_SERVER);

							for (ChatListener listener : chatListeners) {
								listener.onMessageSent(message.getId(), message.getTo());
							}
						}
					} catch (DbException e) {
						LOGGER.log(Level.WARNING, "Failed to handle server ack for messageId : " + message.getId()
								+ "due to database operation failure", e);
					}

				}
			}
		}
	}
}
