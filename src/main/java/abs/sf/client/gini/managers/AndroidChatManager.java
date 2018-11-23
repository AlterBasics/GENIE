package abs.sf.client.gini.managers;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;


import abs.ixi.client.ChatManager;
import abs.ixi.client.core.Callback;
import abs.ixi.client.core.Packet;
import abs.ixi.client.core.Platform;
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
import abs.sf.client.android.db.DbManager;
import abs.sf.client.android.db.object.ChatStoreTable;
import abs.sf.client.android.messaging.ChatLine;
import abs.sf.client.android.messaging.ChatListener;
import abs.sf.client.android.messaging.Conversation;
import abs.sf.client.android.utils.ContextProvider;
import abs.sf.client.android.utils.SFConstants;
import abs.sf.client.android.utils.SharedPrefProxy;

public class AndroidChatManager extends ChatManager {
    private static final Logger LOGGER = Logger.getLogger(AndroidChatManager.getName());

    private static final String MEDIA_RECEIVED_TEXT = "You received a media message";
    private static final String MEDIA_SENT_TEXT = "You sent a media message";

    private List<ChatListene> chatListeners;
    private boolean isChatMarkersEnabled;
    private boolean isChatStateNotificationEnabled;
    private boolean isMessageDeliveryReceiptEnabled;


    public AndroidChatManager(XMPPStreamManager streamManager) {
        super(streamManager);
        this.chatListeners = Collections.synchronizedList(new ArrayList<ChatListener>());
        this.isChatMarkersEnabled = SharedPrefProxy.getInstance().isChatMarkersEnabled();
        this.isChatStateNotificationEnabled = SharedPrefProxy.getInstance().isChatStateNotificationEnabled();
        this.isMessageDeliveryReceiptEnabled = SharedPrefProxy.getInstance().isMessageDeliveryReceiptEnabled();
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
     * @throws InvalidJabberId
     */
    public ChatLine sendTextMessage(String msg, JID toJID, boolean isGroup) {
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
     */
    public ChatLine sendTextMessage(String conversationId, String msg, JID toJID, boolean isGroup) {
        ChatLine chatLine = new ChatLine(conversationId, UUIDGenerator.secureId(), toJID, ChatLine.Direction.SEND);

        chatLine.setContentType(ChatLine.ContentType.TEXT);
        chatLine.setText(msg);

        chatLine.setCreateTime(DateUtils.currentTimeInMiles());
        chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

        chatLine.setMessageStatus(ChatLine.MessageStatus.NOT_DELIVERED_TO_SERVER);

        this.storeChatLine(chatLine);

        this.sendTextMessage(conversationId, chatLine.getMessageId(), chatLine.getText(), toJID, isGroup, isChatMarkersEnabled, isMessageDeliveryReceiptEnabled, isChatStateNotificationEnabled);

        return chatLine;
    }

    /**
     * Use this method to send new media Message. This will send {@link Message} packet to receiver and also trigger media file upload.
     * <p>
     * For retry to upload media file user {@link #reSendMedia(String, File, ContentType, JID, Callback)}.
     * </p>
     *
     * @param mediaFile   {@link File} object of media which have to send
     * @param mediaThumb  thumbnail of media  file
     * @param contentType {@link ContentType} of media
     * @param toJID       media receiver {@link JID}
     * @param isGroup     Is media receiver is group or not
     * @param callback    {@link Callback} to know media transmission status
     * @return
     */
    public ChatLine sendMediaMessage(File mediaFile, byte[] mediaThumb, ContentType contentType, JID toJID, boolean isGroup, Callback<String, FileTransfer.FailureReason> callback) {

        return this.sendMediaMessage(UUIDGenerator.secureId(), mediaFile, mediaThumb, contentType, toJID, isGroup, callback);
    }

    /**
     * Use this method to send new media Message. This will send {@link Message} packet to receiver and also trigger media file upload.
     * <p>
     * <p>
     * For retry to upload media file user {@link #reSendMedia(String, File, ContentType, JID, Callback)}.
     * </p>
     *
     * @param conversationId conversation Id
     * @param mediaFile      {@link File} object of media which have to send
     * @param mediaThumb     thumbnail of media  file
     * @param contentType    {@link ContentType} of media
     * @param toJID          media receiver {@link JID}
     * @param isGroup        Is media receiver is group or not
     * @param callback       {@link Callback} to know media transmission status
     * @return
     */
    public ChatLine sendMediaMessage(String conversationId, File mediaFile, byte[] mediaThumb, ContentType contentType, JID toJID, boolean isGroup, Callback<String, FileTransfer.FailureReason> callback) {
        String mediaMessageId = UUIDGenerator.secureId();
        ChatLine chatLine = new ChatLine(conversationId, mediaMessageId, toJID, ChatLine.Direction.SEND);

        chatLine.setText(MEDIA_SENT_TEXT);

        chatLine.setContentType(ChatLine.ContentType.MEDIA);
        chatLine.setContentId(storeMessageMediaContent(mediaMessageId, mediaThumb, mediaFile, contentType));

        chatLine.setCreateTime(DateUtils.currentTimeInMiles());
        chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

        chatLine.setMessageStatus(ChatLine.MessageStatus.NOT_DELIVERED_TO_SERVER);

        this.storeChatLine(chatLine);

        this.sendMediaMessage(conversationId, mediaMessageId, contentType, Base64.encodeToString(mediaThumb, Base64.DEFAULT), toJID, isGroup, isChatMarkersEnabled, isMessageDeliveryReceiptEnabled, isChatStateNotificationEnabled);

        this.uploadFile(mediaMessageId, mediaFile, contentType, toJID, callback);

        return chatLine;
    }

    /**
     * Use this method to retry to upload media. It will only trigger media file transmission.
     * <p>
     * For New Media message use {@link #sendMediaMessage(String, File, byte[], ContentType, JID, boolean, Callback)}.
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
    public void downloadFile(JID from, final String mediaId, final ContentType contentType, final Callback<File, FileTransfer.FailureReason> callback) {
        LOGGER.info("Downloading media for mediaId");
        this.downloadFile(from, mediaId, contentType, new Callback<File, FileTransfer.FailureReason>() {
            @Override
            public void onSuccess(File file) {
                DbManager.getInstance().updateMediaPath(mediaId, file.getAbsolutePath());
                callback.onSuccess(file);
            }

            @Override
            public void onFailure(FileTransfer.FailureReason failureReason) {
                callback.onFailure(failureReason);
            }
        });
    }

    private long storeMessageMediaContent(String mediaId, byte[] mediaThumb, File mediaFile, ContentType contentType) {
        return DbManager.getInstance().storeMedia(mediaId, mediaThumb, mediaFile.getAbsolutePath(), contentType.getMimeType());
    }

    private void storeChatLine(ChatLine chatLine) {
        DbManager.getInstance().addToChatStore(chatLine);
        DbManager.getInstance().addOrUpdateConversation(chatLine);
    }

    /**
     * This method is called when user has viewed all unSean messages.
     */
    public void sendAllUnReadMessageReadReceipt(JID contactJID) {
        List<ChatLine> unReadChatLines = DbManager.getInstance().getAllUnreadChatLines(contactJID.getBareJID());

        if (!CollectionUtils.isNullOrEmpty(unReadChatLines)) {

            for (ChatLine chatLine : unReadChatLines) {
                this.sendMessageReadReceipt(chatLine);
            }
        }
    }

    /**
     * This method should be called when user has viewed given chatline
     * to let the sender know that receiver has viewed his message.
     *
     * @param chatLine
     */
    public void sendMessageReadReceipt(ChatLine chatLine) {
        try {
            if (chatLine.isMarkable() && !chatLine.haveSean()) {
                this.sendMsgCMDisplayedReceipt(chatLine.getMessageId(),
                        new JID(chatLine.getPeerBareJid()),
                        DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));

                DbManager.getInstance().markMessageViewed(chatLine.getMessageId());
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to send read receipt for messageId : " + chatLine.getMessageId());
        }
    }

    /**
     * This method should be called at the time of notification generation for given chatline
     * to let the sender know that receiver is acknowledged for his message.
     *
     * @param chatLine
     */
    public void sendMessageAcknowledgementReceipt(ChatLine chatLine) {
        try {
            if (chatLine.isMarkable()) {
                this.sendMsgCMAcknowledgedReceipt(chatLine.getMessageId(),
                        new JID(chatLine.getPeerBareJid()),
                        DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to send acknowledgement receipt for messageId : " + chatLine.getMessageId());
        }
    }

    /**
     * This method is called when new message is received
     * to let the sender know that receiver is acknowledged for his message.
     *
     * @param chatLine
     */
    public void sendMessageReceivedReceipt(ChatLine chatLine) {
        try {
            if (chatLine.isMarkable()) {
                this.sendMsgCMReceivedReceipt(chatLine.getMessageId(),
                        new JID(chatLine.getPeerBareJid()),
                        DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));
            }

            if (chatLine.isMdrRequested()) {
                this.sendMsgMDRReceivedReceipt(chatLine.getMessageId(),
                        new JID(chatLine.getPeerBareJid()),
                        DbManager.getInstance().isRosterGroup(chatLine.getPeerBareJid()));
            }

        } catch (Exception e) {
            LOGGER.warning("Failed to send message received receipt for messageId : " + chatLine.getMessageId());
        }
    }

    /**
     * When user has started typing a message for contact,
     * this method should be called to tell contact about it.
     *
     * @param contactJID
     */
    public void sendMessageTypingStarted(JID contactJID) {
        this.sendComposingCSN(contactJID);
    }

    /**
     * When user has paused typing a message for contact,
     * this method should be called to tell contact about it.
     *
     * @param contactJID
     */
    public void sendMessageTypingStopped(JID contactJID) {
        this.sendPausedCSN(contactJID);
    }

    /**
     * When user is not active with contact chat from a shorter time period (eg. since 2 minutes).
     * this method should be called to tell contact about it.
     *
     * @param contactJID
     */
    public void sendChatInactivity(JID contactJID) {
        this.sendInactiveCSN(contactJID);
    }

    /**
     * When user is not active with contact chat from a longer time period (eg. since 10 minutes).
     * this method should be called to tell contact about it.
     *
     * @param contactJID
     */
    public void sendChattingStopped(JID contactJID) {
        this.sendGoneCSN(contactJID);
    }

    /**
     * After reading all messages from contact. then mark no unread conversation available for that contact.
     *
     * @param contactJID
     */
    public void markNoUnreadConversation(JID contactJID) {
        DbManager.getInstance().updateUnreadConversationCount(contactJID.getBareJID(), 0);
    }

    /**
     * @param contactJID
     * @param isGroup
     * @return all conversations for given contact.
     */
    public List<ChatLine> getAllConversationChatLines(JID contactJID, boolean isGroup) {
        return DbManager.getInstance().fetchConversationChatlines(contactJID.getBareJID(), isGroup);
    }

    /**
     * @return All {@link Conversation} list for logged in user.
     */
    public List<Conversation> getAllConversations() {
        return DbManager.getInstance().fetchConversations();
    }

    @Override
    public void collect(final Packet packet) {
        forwardPacket(packet);

        if (packet instanceof Message) {
            Message msg = (Message) packet;

            ChatLine chatLine = prepareIncomingChatLine(msg);

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
                        long mediaId = storeMessageMediaContent(media);
                        chatLine.setContentType(ChatLine.ContentType.MEDIA);
                        chatLine.setContentId(mediaId);
                        chatLine.setText(MEDIA_RECEIVED_TEXT);

                    } else if (content.isContentType(MessageContent.MessageContentType.SUBJECT)) {
                        MessageSubject subject = (MessageSubject) content;

                        if (msg.getType() == Message.MessageType.GROUP_CHAT && !StringUtils.isNullOrEmpty(subject.getSubject())) {
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
                            Long timeInMilles = DateUtils.getDateTime(delay.getStamp(), MessageDelay.TIME_FORMAT, TimeZone.getTimeZone(MessageDelay.UTC_TIMEZONE)).getTime();
                            chatLine.setCreateTime(timeInMilles);
                            chatLine.setDisplayTime(DateUtils.displayTime(timeInMilles));
                        } catch (Exception e) {
                            //Swallow exception
                        }


                    } else if (content.isContentType(MessageContent.MessageContentType.MDR_RECEIVED)) {
                        MDRReceived received = (MDRReceived) content;

                        DbManager.getInstance().markAsReceived(received.getMessageId());

                        for (ChatListener listener : this.chatListeners) {
                            listener.onMessageDeliveredToReceiver(received.getMessageId(), msg.getFrom());
                        }

                        return;

                    } else if (content.isContentType(MessageContent.MessageContentType.CM_RECEIVED)) {
                        CMReceived received = (CMReceived) content;

                        DbManager.getInstance().markAsReceived(received.getMessageId());

                        for (ChatListener listener : this.chatListeners) {
                            listener.onMessageDeliveredToReceiver(received.getMessageId(), msg.getFrom());
                        }

                        return;

                    } else if (content.isContentType(MessageContent.MessageContentType.CM_ACKNOWLEDGED)) {
                        CMAcknowledged acknowledged = (CMAcknowledged) content;
                        DbManager.getInstance().markAsAcknowledged(acknowledged.getMessageId());

                        for (ChatListener listener : this.chatListeners) {
                            listener.onMessageAcknowledgedToReceiver(acknowledged.getMessageId(), msg.getFrom());
                        }

                        return;

                    } else if (content.isContentType(MessageContent.MessageContentType.CM_DISPLAYED)) {
                        CMDisplayed displayed = (CMDisplayed) content;

                        DbManager.getInstance().markAsDisplayed(displayed.getMessageId(), chatLine.getPeerBareJid());

                        for (ChatListener listener : this.chatListeners) {
                            listener.onMessageViewedByReceiver(displayed.getMessageId(), msg.getFrom());
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

                synchronized (ChatStoreTable.class) {
                    boolean isMessageAlreadyExsist = DbManager.getInstance().isMessageAlreadyExist(chatLine.getPeerBareJid(), chatLine.getMessageId());

                    if (!isMessageAlreadyExsist) {
                        storeChatLine(chatLine);

                        sendMessageReceivedReceipt(chatLine);

                        generateLocalBroadcast(chatLine);

                        notifyOnChatLine(chatLine);
                    }
                }
            }

        }
    }

    private long storeMessageMediaContent(MessageMedia media) {
        return DbManager.getInstance().storeMedia(media.getMediaId(), Base64.decode(media.getThumb(), Base64.DEFAULT), null, media.getContentType().getMimeType());
    }

    private void changeChatRoomSubject(JID roomJID, String subject) {
        DbManager.getInstance().updateChatRoomSubject(roomJID.getBareJID(), subject);
        //TODO: may send subject change to listener to reflect on UI at run time.
    }

//    private ChatLine prepareChatLine(Message msg) {
//        String from = msg.getFrom().getBareJID();
//
//        ChatLine chatLine = new ChatLine(msg.getId(), from, ChatLine.Direction.RECEIVE);
//        chatLine.setMessageStatus(ChatLine.MessageStatus.INCOMMING_MESSAGE); //-1 indicates received messages
//
//        if (msg.getFrom().getResource() != null) {
//            chatLine.setPeerResource(msg.getFrom().getResource().trim());
//        }
//
//        chatLine.setPeerName(msg.getFrom().getNode());
//
//        chatLine.setCreateTime(DateUtils.currentTimeInMiles());
//        chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));
//
//        if (msg.getType() == Message.MessageType.GROUP_CHAT) {
//            String memberNickName = chatLine.getPeerResource();
//            JID memberJID = new JID(memberNickName, Platform.getInstance().getSession().get(Session.KEY_DOMAIN).toString());
//
//            String userName = DbManager.getInstance().getRosterItemName(memberJID.getBareJID());
//
//            if (StringUtils.isNullOrEmpty(userName)) {
//                chatLine.setPeerName(memberNickName);
//
//            } else {
//                chatLine.setPeerName(userName);
//            }
//        }
//
//        return chatLine;
//    }

    private ChatLine prepareIncomingChatLine(Message msg) {
        String from = msg.getFrom().getBareJID();

        ChatLine chatLine = new ChatLine(msg.getId(), from, ChatLine.Direction.RECEIVE);
        chatLine.setMessageStatus(ChatLine.MessageStatus.INCOMMING_MESSAGE); //-1 indicates received messages

        if (msg.getFrom().getResource() != null) {
            chatLine.setPeerResource(msg.getFrom().getResource().trim());
        }
        chatLine.setCreateTime(DateUtils.currentTimeInMiles());
        chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

        if (msg.getType() == Message.MessageType.GROUP_CHAT) {
            String memberNickName = chatLine.getPeerResource();
            String memberJID = DbManager.getInstance().getChatRoomMemberJID(msg.getFrom().getBareJID(), memberNickName);

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
    }

    private void notifyOnChatLine(ChatLine chatLine) {
        if (!CollectionUtils.isNullOrEmpty(this.chatListeners)) {
            for (ChatListener listener : this.chatListeners) {
                listener.onNewMessageReceived(chatLine);
            }
        }
    }

    private static void generateLocalBroadcast(final ChatLine chatLine) {
        ContextProvider contextProvider = (ContextProvider) Platform.getInstance().getSession().get(ContextProvider.KEY_CONTEXT);
        final Context context = contextProvider.context();
        Intent intent = new Intent(SFConstants.ACTION_ON_MESSAGE);
        intent.putExtra(SFConstants.CHATLINE_OBJECT, chatLine);
        context.sendBroadcast(intent);
    }


    public class MessageAckHandler implements EventHandler {
        @Override
        public void handle(Event e) {
            List<Message> messages = (List<Message>) e.getSource();

            if (!CollectionUtils.isNullOrEmpty(messages)) {

                for (Message message : messages) {
                    if (!DbManager.getInstance().isMessageAlreadyDelivered(message.getId())) {

                        DbManager.getInstance().updateDeliveryStatus(message.getId(), ChatLine.MessageStatus.DELIVERED_TO_SERVER);

                        for (ChatListener listener : chatListeners) {
                            listener.onMessageSent(message.getId(), message.getTo());
                        }
                    }
                }
            }
        }
    }
}
