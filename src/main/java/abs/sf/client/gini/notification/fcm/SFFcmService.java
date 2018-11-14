package abs.sf.client.gini.notification.fcm;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.core.Platform;
import abs.ixi.client.file.sfcm.ContentType;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xml.XMLUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.Message;
import abs.ixi.client.xmpp.packet.MessageBody;
import abs.ixi.client.xmpp.packet.MessageContent;
import abs.ixi.client.xmpp.packet.MessageDelay;
import abs.ixi.client.xmpp.packet.MessageMedia;
import abs.sf.client.android.db.DbManager;
import abs.sf.client.android.db.object.ChatStoreTable;
import abs.sf.client.android.db.object.ConversationTable;
import abs.sf.client.android.messaging.ChatLine;
import abs.sf.client.android.messaging.ChatListener;
import abs.sf.client.android.messaging.Conversation;
import abs.sf.client.android.notification.SFNotifiactionCode;
import abs.sf.client.android.utils.ContextProvider;
import abs.sf.client.android.utils.SFConstants;
import abs.sf.client.android.utils.SharedPrefProxy;

public class SFFcmService extends FirebaseMessagingService {
    private static final Logger LOGGER = Logger.getLogger(SFFcmService.class
            .getName());

    private static final String CONVERSATION_ID = "conversation_id";
    private static final String MESSAGE_ID = "message_id";
    private static final String FROM_JID = "from_jid";
    private static final String MESSAGE = "message";
    private static final String MEDIA_ID = "media_id";
    private static final String MEDIA_THUMB = "media_thumb";
    private static final String CONTENT_TYPE = "content_type";
    private static final String IS_MARKABLE_MESASGE = "is_markable_message";
    private static final String IS_MDR_REQUESTED = "is_mdr_requested";
    private static final String TIMESTAMP = "timeStamp";
    private static final String TRUE = "true";

    private static final String MEDIA_RECEIVED_TEXT = "You received a media message";

    private static final List<ChatListener> CHAT_LISTENERS = Collections.synchronizedList(new ArrayList<ChatListener>());

    public static void addChatListener(ChatListener chatListener) {
        CHAT_LISTENERS.add(chatListener);
    }

    public static void removeChatListener(ChatListener chatListener) {
        CHAT_LISTENERS.remove(chatListener);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleNotification(remoteMessage, getApplicationContext());
    }

    public static boolean handleNotification(RemoteMessage remoteMessage, Context context) {
        try {
            if (isSFMessage(remoteMessage)) {
                LOGGER.log(Level.INFO, "Handaling Stringflow push notification received");

                Platform.getInstance().getSession().put(ContextProvider.KEY_CONTEXT, context);

                SFNotifiactionCode sfCode = getSFNotificationCode(remoteMessage);

                if (sfCode != null) {

                    if (sfCode == SFNotifiactionCode.TEXT_MESSAGE
                            || sfCode == SFNotifiactionCode.MEDIA_MESSAGE) {

                        handleMessageNotification(remoteMessage, context);

                    }
                }

                return true;

            } else {
                LOGGER.log(Level.INFO, "Not a Stringflow related notification, So not handaling it");
                return false;
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to handle  SF push notification ", e);
        }

        return false;
    }

    private static void handleMessageNotification(RemoteMessage remoteMessage, Context context) throws Exception {

        SFNotifiactionCode sfCode = getSFNotificationCode(remoteMessage);

        Map<String, String> dataMap = remoteMessage.getData();

        String conversationId = dataMap.get(CONVERSATION_ID);
        String messageId = dataMap.get(MESSAGE_ID);

        JID fromJID = new JID(dataMap.get(FROM_JID));
        String message = XMLUtils.decodeSpecialChars(MESSAGE);

        String mediaId = dataMap.get(MEDIA_ID);
        String mediaThumb = dataMap.get(MEDIA_THUMB);
        String mediaContentType = dataMap.get(CONTENT_TYPE);

        boolean isMarkableMessage = StringUtils.safeEquals(dataMap.get(IS_MARKABLE_MESASGE), TRUE);
        boolean isMDRRequested = StringUtils.safeEquals(dataMap.get(IS_MDR_REQUESTED), TRUE);

        String timeStamp = dataMap.get(TIMESTAMP);


        ChatLine chatLine = new ChatLine(messageId, fromJID.getBareJID(), ChatLine.Direction.RECEIVE);
        chatLine.setMessageStatus(ChatLine.MessageStatus.INCOMMING_MESSAGE); //-1 indicates received messages

        if (fromJID.getResource() != null) {
            chatLine.setPeerResource(fromJID.getResource().trim());
        }

        chatLine.setCreateTime(DateUtils.currentTimeInMiles());
        chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

        if (DbManager.getInstance().isRosterGroup(fromJID.getBareJID())) {
            String memberNickName = chatLine.getPeerResource();
            String memberJID = DbManager.getInstance().getChatRoomMemberJID(fromJID.getBareJID(), memberNickName);

            String userName = DbManager.getInstance().getRosterItemName(memberJID);

            if (StringUtils.isNullOrEmpty(userName)) {
                chatLine.setPeerName(memberNickName);

            } else {
                chatLine.setPeerName(userName);
            }

        } else {
            String userName = DbManager.getInstance().getRosterItemName(fromJID.getBareJID());

            if (StringUtils.isNullOrEmpty(userName)) {
                chatLine.setPeerName(fromJID.getNode());

            } else {
                chatLine.setPeerName(userName);
            }
        }

        if (!StringUtils.isNullOrEmpty(message)) {
            chatLine.setContentType(ChatLine.ContentType.TEXT);
            chatLine.setText(message);

        }

        if (!StringUtils.isNullOrEmpty(conversationId)) {
            chatLine.setConversationId(conversationId);
        }

        if (sfCode == SFNotifiactionCode.MEDIA_MESSAGE && !StringUtils.isNullOrEmpty(mediaId)) {
            long mediaUUId = storeMessageMediaContent(mediaId, mediaThumb, mediaContentType);
            chatLine.setContentType(ChatLine.ContentType.MEDIA);
            chatLine.setContentId(mediaUUId);
            chatLine.setText(MEDIA_RECEIVED_TEXT);
        }

        if (isMDRRequested) {
            chatLine.setMdrRequested(true);

        }

        if (isMarkableMessage) {
            chatLine.setMarkable(true);

        }

        if (StringUtils.isNullOrEmpty(timeStamp)) {
            try {
                Long timeInMilles = DateUtils.getDateTime(timeStamp, MessageDelay.TIME_FORMAT, TimeZone.getTimeZone(MessageDelay.UTC_TIMEZONE)).getTime();
                chatLine.setCreateTime(timeInMilles);
                chatLine.setDisplayTime(DateUtils.displayTime(timeInMilles));
            } catch (Exception e) {
                //Swallow exception
            }
        }

        synchronized (ChatStoreTable.class) {
            boolean isMessageAlreadyExsist = DbManager.getInstance().isMessageAlreadyExist(chatLine.getPeerBareJid(), chatLine.getMessageId());

            if (!isMessageAlreadyExsist) {
                storeChatLine(chatLine);

                generateLocalBroadcast(chatLine, context);

                notifyOnChatLine(chatLine);
            }
        }

    }

    private static long storeMessageMediaContent(String mediaId, String mediaThumb, String contentType) {
        return DbManager.getInstance().storeMedia(mediaId, Base64.decode(mediaThumb, Base64.DEFAULT), null, contentType);
    }

    private static void storeChatLine(ChatLine chatLine) {
        DbManager.getInstance().addToChatStore(chatLine);
        DbManager.getInstance().addOrUpdateConversation(chatLine);
    }

    private static void generateLocalBroadcast(final ChatLine chatLine, Context context) {
        Intent intent = new Intent(SFConstants.ACTION_ON_MESSAGE);
        intent.putExtra(SFConstants.CHATLINE_OBJECT, chatLine);
        context.sendBroadcast(intent);
    }

    private static void notifyOnChatLine(final ChatLine chatLine) {
        if (!CollectionUtils.isNullOrEmpty(CHAT_LISTENERS)) {
            for (ChatListener receiver : CHAT_LISTENERS) {
                receiver.onNewMessageReceived(chatLine);
            }
        }
    }

    private static SFNotifiactionCode getSFNotificationCode(RemoteMessage remoteMessage) {
        String sfNotificationCode = remoteMessage.getData().get(SFNotifiactionCode.SF_NOTIFICATION_CODE);
        int code = Integer.parseInt(sfNotificationCode);
        return SFNotifiactionCode.valueFrom(code);
    }

    public static boolean isSFMessage(RemoteMessage remoteMessage) {
        if (remoteMessage == null || remoteMessage.getData() == null) {
            return false;
        }

        Map<String, String> dataMap = remoteMessage.getData();

        String sfNotificationCode = dataMap.get(SFNotifiactionCode.SF_NOTIFICATION_CODE);

        return !StringUtils.isNullOrEmpty(sfNotificationCode);
    }
}
