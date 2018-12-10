package abs.sf.client.gini.messaging;

import abs.ixi.client.xmpp.JID;

public interface ChatListener {

    /**
     * This method is used to handle new received Message on UI.
     * @param line
     */
    void onNewMessageReceived(final ChatLine line);

    /**
     * When message is delivered from user device, this method is called to notify delivery on UI.
     *
     * @param messageId
     * @param contactJID
     */
    void onMessageSent(final String messageId, final JID contactJID);

    /**
     * When message is delivered to message receiver, this method is called to notify on UI.
     *
      * @param messageId
     * @param contactJID
     */
    void onMessageDeliveredToReceiver(final String messageId, final JID contactJID);

    /**
     * When message is acknowledged to received by notification, this method is called to notify on UI.
     *
     * @param messageId
     * @param contactJID
     */
    void onMessageAcknowledgedToReceiver(final String messageId, final JID contactJID);


    /**
     * When message is sean by receiver, this method is called to notify on UI.
     *
     * @param messageId
     * @param contactJID
     */
    void onMessageViewedByReceiver(final String messageId, final JID contactJID);

    /**
     * When user's Contact is typing a message for user, this method is called to show typing status on UI.
     *
     * @param contactJID
     */
    void onContactTypingStarted(final JID contactJID);

    /**
     * When user's Contact has paused typing a message for user, this method is called to show typing status on UI.
     *
     * @param contactJID
     */
    void onContactTypingPaused(final JID contactJID);

    /**
     *When user's contact is not active with user from a shorter time period (eg. since 2 minutes). this method is called notify on UI.
     *
     * @param contactJID
     */
    void onContactInactivityInUserChat(final JID contactJID);

    /**
     *When user's contact is not active with user from a longer time period (eg. since 10 minutes).  this method is called notify on UI.
     *
     * @param contactJID
     */
    void onContactGoneFromUserChat(final JID contactJID);
}
