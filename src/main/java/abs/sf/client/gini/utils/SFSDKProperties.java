package abs.sf.client.gini.utils;

import java.io.IOException;

import abs.ixi.client.PushNotificationService;
import abs.ixi.client.util.StringUtils;

public class SFSDKProperties {
	public static final String SDK_PROPERTIES_FILE = "sf_android.properties";
	public static final String ROSTER_VERSION = "roster_version";
	public static final String DEVIICE_TOKEN = "device_token";
	public static final String NOTIFICATION_SERVICE = "notification_service";
	public static final String DOMAIN_NAME = "domain_name";
	public static final String IS_CHAT_MARKERS_SUPPORTED = "is_chat_markers_supported";
	public static final String IS_MESSAGE_DELIVERY_RECEIPT_SUPPORTED = "is_message_delivery_receipt_supported";
	public static final String IS_CHAT_STATE_NOTIFICATION_SUPPORTED = "is_chat_state_notification_supported";

	private SFProperties sfProperties;
	public static SFSDKProperties instance;

	/**
	 * Restricting access to local
	 * 
	 * @throws IOException
	 */
	private SFSDKProperties() throws IOException {
		this.sfProperties = new SFProperties(SDK_PROPERTIES_FILE);
	}

	/**
	 * Returns the singleton instance of {@code SFSDKProperties}
	 */
	public static SFSDKProperties getInstance() {
		if (instance == null) {
			try {

				instance = new SFSDKProperties();

			} catch (IOException e) {
				
				throw new RuntimeException(e);
			}
		}

		return instance;
	}

	public void setRosterVersion(int version) {
		this.sfProperties.getEditor().putInt(ROSTER_VERSION, version).apply();
	}

	public int getRosterVersion() {
		return sfProperties.getInt(ROSTER_VERSION, 0);
	}

	public String getDeviceToken() {
		return this.get(DEVIICE_TOKEN);
	}

	public PushNotificationService getNotificationService() {
		String service = this.get(NOTIFICATION_SERVICE);

		try {

			return PushNotificationService.valueOf(service);

		} catch (IllegalArgumentException e) {
			return null;
		}

	}

	public void savePushNotifiactionDetatils(PushNotificationService service, String deviceToken) {
		this.sfProperties.getEditor().put(NOTIFICATION_SERVICE, service.name()).put(DEVIICE_TOKEN, deviceToken).apply();

	}

	public void setDomainName(String domainName) {
		this.sfProperties.getEditor().putString(DOMAIN_NAME, domainName).apply();
	}

	public String getDomainName() {
		return this.get(DOMAIN_NAME);
	}

	/**
	 * Enable Chat State Notifications. Which will make typing status visible of
	 * any contact member.
	 * <p>
	 * Please refer XEP-0085.
	 * </p>
	 */
	public void enableChatStateNotification() {
		this.setIsChatStateNotificationSupported(true);
	}

	/**
	 * Enable Message Delivery Receipt.By enabling it we can see that message is
	 * delivered to contact or not.
	 * <p>
	 * Please refer XEP-0184.
	 * </p>
	 */
	public void enableMessageDeliveryReceipt() {
		this.setIsMessageDeliveryReceiptSupported(true);
	}

	/**
	 * Enable Chat-Markers. By enabling it we can see message delivery status
	 * and seen unseen by contact.
	 * <p>
	 * Please refer XEP-0333.
	 * </p>
	 *
	 * <p>
	 * This XEP is deferred by xmpp org. But we still support it. If we are not
	 * interested in message sean by receiver and only interested to delivery
	 * status then use {@link #enableMessageDeliveryReceipt()}.
	 * </p>
	 */
	public void enableChatMarkers() {
		this.setIsChatMarkersSupported(true);
	}

	/**
	 * Disable Chat State Notifications. Which will make typing status invisible
	 * of any contact member.
	 * <p>
	 * Please refer XEP-0085.
	 * </p>
	 */
	public void disableChatStateNotification() {
		this.setIsChatStateNotificationSupported(false);
	}

	/**
	 * Disable Message Delivery Receipt.By disabling it we insure that we are
	 * not interested in message delivery status.
	 * <p>
	 * Please refer XEP-0184.
	 * </p>
	 */
	public void disableMessageDeliveryReceipt() {
		this.setIsMessageDeliveryReceiptSupported(false);
	}

	/**
	 * disable Chat-Markers. By disabling it we insure that we are not
	 * interested in message delivery status and seen unseen by contact.
	 * <p>
	 * Please refer XEP-0333.
	 * </p>
	 *
	 */
	public void disableChatMarkers() {
		this.setIsChatMarkersSupported(false);
	}

	/**
	 * Enable Chat State Notifications by setting true. Which will make typing
	 * status visible of any contact member. by default it is false.
	 * <p>
	 * Please refer XEP-0085.
	 * </p>
	 *
	 */
	public void setIsChatStateNotificationSupported(boolean isChatStateNotificationSupported) {
		this.sfProperties.getEditor().putBoolean(IS_CHAT_STATE_NOTIFICATION_SUPPORTED, isChatStateNotificationSupported)
				.apply();
	}

	/**
	 * Enable Message Delivery Receipt.By enabling it we can see that message is
	 * delivered to contact or not. By default this is disabled.
	 * <p>
	 * Please refer XEP-0184.
	 * </p>
	 */
	public void setIsMessageDeliveryReceiptSupported(boolean isMessageDeliveryReceiptSupported) {
		this.sfProperties.getEditor()
				.putBoolean(IS_MESSAGE_DELIVERY_RECEIPT_SUPPORTED, isMessageDeliveryReceiptSupported).apply();
	}

	/**
	 * Enable Chat-Markers. By enabling it we can see message delivery status
	 * and seen unseen by contact. By default this is disabled.
	 * <p>
	 * Please refer XEP-0333.
	 * </p>
	 *
	 * <p>
	 * This XEP is deferred by xmpp org. But we still support it. If we are not
	 * interested in message sean by receiver and only interested to delivery
	 * status then use {@link #enableMessageDeliveryReceipt()}.
	 * </p>
	 */
	public void setIsChatMarkersSupported(boolean isChatMarkersSupported) {
		this.sfProperties.getEditor().putBoolean(IS_CHAT_MARKERS_SUPPORTED, isChatMarkersSupported).apply();
	}

	public boolean isChatStateNotificationSupported() {
		return this.sfProperties.getBoolean(IS_CHAT_STATE_NOTIFICATION_SUPPORTED, false);
	}

	public boolean isMessageDeliveryReceiptSupported() {
		return this.sfProperties.getBoolean(IS_MESSAGE_DELIVERY_RECEIPT_SUPPORTED, false);
	}

	public boolean isChatMarkersSupported() {
		return this.sfProperties.getBoolean(IS_CHAT_MARKERS_SUPPORTED, false);
	}

	public String get(String key) {
		return this.sfProperties.getProperty(key, StringUtils.EMPTY);
	}

	public void remove(String key) {
		this.sfProperties.getEditor().remove(key).apply();
	}

	/**
	 * Removes all the preferences from the object it is editing.
	 */
	public void clear() {
		this.sfProperties.getEditor().clear().apply();
	}

}
