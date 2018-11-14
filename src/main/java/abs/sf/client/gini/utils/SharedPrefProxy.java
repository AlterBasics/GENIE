package abs.sf.client.gini.utils;

import android.content.Context;
import android.content.SharedPreferences;

import abs.ixi.client.PushNotificationService;
import abs.ixi.client.core.Platform;
import abs.ixi.client.util.StringUtils;

/**
 * As the name suggests, {@code SharedPrefProxy} is a proxy interface to
 * Android {@code SharedPreferences}. This is a singleton class.
 * <p>
 * All the preferences stored using this class are private.
 * </p>
 */
public final class SharedPrefProxy {
    public static final String USER_PREFS = "sf_android_sdk_user_prefs";
    public static final String ROSTER_VERSION = "roster_version";
    public static final String DEVIICE_TOKEN = "device_token";
    public static final String NOTIFICATION_SERVICE = "notification_service";
    public static final String DOMAIN_NAME = "domain_name";
    public static final String IS_CHAT_MARKERS_SUPPORTED = "is_chat_markers_supported";
    public static final String IS_MESSAGE_DELIVERY_RECEIPT_SUPPORTED ="is_message_delivery_receipt_supported";
    public static final String IS_CHAT_STATE_NOTIFICATION_SUPPORTED = "is_chat_state_notification_supported";

    private SharedPreferences systemPrefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public static SharedPrefProxy instance;

    /**
     * Restricting access to local
     */
    private SharedPrefProxy() {
        this.context = (Context) Platform.getInstance().getSession().get(ContextProvider.KEY_CONTEXT);
        this.systemPrefs = context.getSharedPreferences(USER_PREFS, context.MODE_PRIVATE);
        this.editor = systemPrefs.edit();
    }

    /**
     * Returns the singleton instance of {@code SharedPrefProxy}
     */
    public static SharedPrefProxy getInstance() {
        if (instance == null) {
            instance = new SharedPrefProxy();
        }

        return instance;
    }

    public void setRosterVersion(int version) {
        this.editor.putInt(ROSTER_VERSION, version);
        this.editor.commit();
    }

    public int getRosterVersion() {
        return systemPrefs.getInt(ROSTER_VERSION, 0);
    }

    public String getDeviceToken() {
        return this.get(DEVIICE_TOKEN);
    }

    public PushNotificationService getNotificationService() {
        String service =  this.get(NOTIFICATION_SERVICE);
        try {

            return PushNotificationService.valueOf(service);

        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    public void savePushNotifiactionDetatils(PushNotificationService service, String deviceToken) {
        if(service != null) {
            this.put(NOTIFICATION_SERVICE, service.name());
            this.put(DEVIICE_TOKEN, deviceToken);
        }

        this.editor.commit();
    }

    public void setDomainName(String domainName) {
        this.editor.putString(DOMAIN_NAME, domainName);
        this.editor.commit();
    }

    public String getDomainName() {
        return this.get(DOMAIN_NAME);
    }

    /**
     * Enable Chat State Notifications. Which will make typing status visible of any contact member.
     * <p>
     *     Please refer XEP-0085.
     *</p>
     */
    public void enableChatStateNotification() {
        this.setIsChatStateNotificationSupported(true);
    }

    /**
     * Enable Message Delivery Receipt.By enabling it we can see that message is delivered to contact or not.
     * <p>
     *    Please refer XEP-0184.
     * </p>
     */
    public void enableMessageDeliveryReceipt() {
        this.setIsMessageDeliveryReceiptSupported(true);
    }

    /**
     * Enable Chat-Markers. By enabling it we can see message delivery status and seen unseen by contact.
     * <p>
     *     Please refer XEP-0333.
     * </p>
     *
     * <p>
     *     This XEP is deferred by xmpp org. But we still support it.
     *     If we are not interested in message sean by receiver and only interested to delivery status then use {@link #enableMessageDeliveryReceipt()}.
     * </p>
     */
    public void enableChatMarkers() {
        this.setIsChatMarkersSupported(true);
    }

    /**
     * Disable Chat State Notifications. Which will make typing status invisible of any contact member.
     * <p>
     * Please refer XEP-0085.
     *</p>
     */
    public void disableChatStateNotification() {
        this.setIsChatStateNotificationSupported(false);
    }

    /**
     * Disable Message Delivery Receipt.By disabling it we insure that we are not interested in message delivery status.
     * <p>
     *    Please refer XEP-0184.
     * </p>
     */
    public void disableMessageDeliveryReceipt() {
        this.setIsMessageDeliveryReceiptSupported(false);
    }

    /**
     * disable Chat-Markers. By disabling it we insure that we are not interested in message delivery status and seen unseen by contact.
     * <p>
     *     Please refer XEP-0333.
     * </p>
      *
     */
    public void disableChatMarkers() {
        this.setIsChatMarkersSupported(false);
    }

    /**
     * Enable Chat State Notifications by setting true. Which will make typing status visible of any contact member.
     * by default it is false.
     * <p>
     * Please refer XEP-0085.
     *</p>
     *
     */
    public void setIsChatStateNotificationSupported(boolean isChatStateNotificationSupported) {
        this.editor.putBoolean(IS_CHAT_STATE_NOTIFICATION_SUPPORTED, isChatStateNotificationSupported);
        this.editor.commit();
    }

    /**
     * Enable Message Delivery Receipt.By enabling it we can see that message is delivered to contact or not.
     * By default this is disabled.
     * <p>
     *    Please refer XEP-0184.
     * </p>
     */
    public void setIsMessageDeliveryReceiptSupported(boolean isMessageDeliveryReceiptSupported) {
        this.editor.putBoolean(IS_MESSAGE_DELIVERY_RECEIPT_SUPPORTED, isMessageDeliveryReceiptSupported);
        this.editor.commit();
    }

    /**
     * Enable Chat-Markers. By enabling it we can see message delivery status and seen unseen by contact.
     * By default this is disabled.
     * <p>
     *     Please refer XEP-0333.
     * </p>
     *
     * <p>
     *     This XEP is deferred by xmpp org. But we still support it.
     *     If we are not interested in message sean by receiver and only interested to delivery status then use {@link #enableMessageDeliveryReceipt()}.
     * </p>
     */
    public void setIsChatMarkersSupported(boolean isChatMarkersSupported) {
        this.editor.putBoolean(IS_CHAT_MARKERS_SUPPORTED, isChatMarkersSupported);
        this.editor.commit();
    }

    public boolean isChatStateNotificationSupported() {
        return this.systemPrefs.getBoolean(IS_CHAT_STATE_NOTIFICATION_SUPPORTED, false);
    }

    public boolean isMessageDeliveryReceiptSupported () {
        return this.systemPrefs.getBoolean(IS_MESSAGE_DELIVERY_RECEIPT_SUPPORTED, false);
    }

    public boolean isChatMarkersSupported() {
        return this.systemPrefs.getBoolean(IS_CHAT_MARKERS_SUPPORTED, false);
    }

    /**
     * Stores value into user shared preferences
     */
    public void put(String key, String val) {
        this.editor.putString(key, val);
        this.editor.commit();
    }

    public String get(String key) {
        return systemPrefs.getString(key, StringUtils.EMPTY);
    }

    public void remove(String key) {
        this.editor.remove(key);
        this.editor.apply();
    }

    /**
     * Removes all the preferences from the object it is editing.
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

}
