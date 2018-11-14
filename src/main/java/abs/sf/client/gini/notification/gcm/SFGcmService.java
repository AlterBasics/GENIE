package abs.sf.client.gini.notification.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import abs.ixi.client.core.Platform;
import abs.ixi.client.core.Session;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.DateUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.InvalidJabberId;
import abs.ixi.client.xmpp.JID;
import abs.sf.client.android.db.DbManager;
import abs.sf.client.android.db.object.ChatStoreTable;
import abs.sf.client.android.db.object.ConversationTable;
import abs.sf.client.android.messaging.ChatLine;
import abs.sf.client.android.messaging.ChatListener;
import abs.sf.client.android.messaging.Conversation;
import abs.sf.client.android.notification.SFNotifiactionCode;
import abs.sf.client.android.utils.SFConstants;


public class SFGcmService extends GcmListenerService{
    private static final String POLL_RECEIVED_TEXT = "POLL";
    private static final String MEDIA_RECEIVED_TEXT = "MEDIA";
    private static final List<ChatListener> CHAT_LISTENERS = Collections.synchronizedList(new ArrayList<ChatListener>());

    public static void addChatListener(ChatListener chatListener) {
        CHAT_LISTENERS.add(chatListener);
    }

    public static void removeChatListener(ChatListener chatListener) {
        CHAT_LISTENERS.remove(chatListener);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        handleNotification(data, getApplicationContext());
    }

    public static boolean handleNotification(Bundle remoteMessage, Context context) {
        if (isSFMessage(remoteMessage)) {
            SFNotifiactionCode sfCode = getSFNotificationCode(remoteMessage);

            if(sfCode != null) {

                if (sfCode == SFNotifiactionCode.TEXT_MESSAGE
                        || sfCode == SFNotifiactionCode.MEDIA_MESSAGE) {

                    handleMessageNotification(remoteMessage, context);

                }
            }

            return true;
        }

        return false;
    }

    private static void handleMessageNotification(Bundle remoteMessage, Context context) {
        SFNotifiactionCode sfCode = getSFNotificationCode(remoteMessage);

        try {

            String from = remoteMessage.getString("from_jid");
            String message = remoteMessage.getString("message");
            String messageId = remoteMessage.getString("message_id");
            String markable = remoteMessage.getString("markable");
            String createTime = remoteMessage.getString("createTime");

            boolean isMarkable = false;

            if(StringUtils.safeEquals(markable, "true"))
                isMarkable = true;


            JID fromJID = new JID(from);

            ChatLine chatLine = new ChatLine(messageId, fromJID.getBareJID(), ChatLine.Direction.RECEIVE);
            chatLine.setMessageStatus(ChatLine.MessageStatus.INCOMMING_MESSAGE); //-1 indicates received messages

            if(fromJID.getResource() != null) {
                chatLine.setPeerResource(fromJID.getResource().trim());
            }

            chatLine.setPeerName(fromJID.getNode());

            chatLine.setCreateTime(DateUtils.currentTimeInMiles());
            chatLine.setDisplayTime(DateUtils.displayTime(DateUtils.currentTimeInMiles()));

            if(StringUtils.isNullOrEmpty(createTime)) {
                try {
                    Long timeInMilles = Long.parseLong(createTime);
                    chatLine.setCreateTime(timeInMilles);
                    chatLine.setDisplayTime(DateUtils.displayTime(timeInMilles));
                } catch (Exception e) {
                    //Swellow exception
                }
            }

            chatLine.setMarkable(isMarkable);

            if (DbManager.getInstance().isRosterGroup(fromJID.getBareJID())) {
                String memberNickName = chatLine.getPeerResource();
                JID memberJID = new JID(memberNickName, Platform.getInstance().getSession().get(Session.KEY_DOMAIN).toString());

                String userName = DbManager.getInstance().getRosterItemName(memberJID.getBareJID());

                if (StringUtils.isNullOrEmpty(userName)) {
                    chatLine.setPeerName(memberNickName);

                } else {
                    chatLine.setPeerName(userName);
                }
            }

            if(sfCode == SFNotifiactionCode.TEXT_MESSAGE) {
                chatLine.setContentType(ChatLine.ContentType.TEXT);
                chatLine.setText(message);

            } if(sfCode == SFNotifiactionCode.MEDIA_MESSAGE) {
                long mediaId = 0;

                //TODO: handle it
//                MessageMedia media = (MessageMedia) msg.getContent();
//                long mediaId = storeMedia(media);

                chatLine.setContentType(ChatLine.ContentType.MEDIA);
                chatLine.setContentId(mediaId);
                chatLine.setText(MEDIA_RECEIVED_TEXT);

            }

            synchronized (ChatStoreTable.class) {
                boolean isMessageAlreadyExsist = DbManager.getInstance().isMessageAlreadyExist(fromJID.getBareJID(), messageId);

                if (!isMessageAlreadyExsist) {
                    storeChatLine(chatLine);

                    notifyOnChatLine(chatLine);
                    generateLocalBroadcast(chatLine, context);
                }
            }

            //TODO: Generate notification

        } catch (InvalidJabberId e) {
            //Swallow Exception
        }
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
        if(!CollectionUtils.isNullOrEmpty(CHAT_LISTENERS)) {
            for(ChatListener receiver : CHAT_LISTENERS) {
                receiver.onNewMessageReceived(chatLine);
            }
        }
    }

    private static SFNotifiactionCode getSFNotificationCode(Bundle remoteMessage) {
        String sfNotificationCode = remoteMessage.getString(SFNotifiactionCode.SF_NOTIFICATION_CODE);
        int code = Integer.parseInt(sfNotificationCode);
        return SFNotifiactionCode.valueFrom(code);
    }

    public static boolean isSFMessage(Bundle remoteMessage) {
        if (remoteMessage == null || remoteMessage.getString(SFNotifiactionCode.SF_NOTIFICATION_CODE) == null) {
            return false;
        }

        String sfNotificationCode = remoteMessage.getString(SFNotifiactionCode.SF_NOTIFICATION_CODE);

        return  !StringUtils.isNullOrEmpty(sfNotificationCode);
    }

}
