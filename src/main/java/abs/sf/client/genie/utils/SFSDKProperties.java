package abs.sf.client.genie.utils;

import abs.ixi.client.Platform;
import abs.ixi.client.PushNotificationService;
import abs.ixi.client.util.StringUtils;
import abs.sf.client.genie.exception.StringflowErrorException;
import abs.sf.client.genie.managers.AppChatManager;

public class SFSDKProperties {
	private static final String SDK_PROPERTIES_RESOURCE = "conf/sf_sdk.properties";

	private static final String ROSTER_VERSION = "roster_version";
	private static final String DEVIICE_TOKEN = "device_token";
	private static final String NOTIFICATION_SERVICE = "notification_service";
	private static final String IS_CHAT_MARKERS_ENABLED = "is_chat_markers_enabled";
	private static final String IS_MESSAGE_DELIVERY_RECEIPT_ENABLED = "is_message_delivery_receipt_enabled";
	private static final String IS_CHAT_STATE_NOTIFICATION_ENABLED = "is_chat_state_notification_enabled";
	private static final String IS_MEDIA_TRANSFER_ENABLED = "is_media_transfer_enabled";

	private static final String H2_DB_FILE_PATH = "H2_DB_FILE_PATH";

	private SFProperties sfProperties;
	public static SFSDKProperties instance;

	/**
	 * Restricting access to local
	 * 
	 * @throws StringflowErrorException
	 */
	private SFSDKProperties() throws StringflowErrorException {
		this.sfProperties = new SFProperties(SDK_PROPERTIES_RESOURCE);
	}

	/**
	 * Returns the singleton instance of {@code SFSDKProperties}
	 * 
	 * @throws StringflowErrorException
	 */
	public static SFSDKProperties getInstance() throws StringflowErrorException {
		if (instance == null) {
			synchronized (SFSDKProperties.class) {
				if (instance == null) {
					instance = new SFSDKProperties();
				}
			}
		}

		return instance;
	}

	public void setH2DbFilePath(String h2DbFilePath) throws StringflowErrorException {
		this.sfProperties.getEditor().putString(H2_DB_FILE_PATH, h2DbFilePath).commit();
	}

	public String getH2DbFilePath() {
		return this.sfProperties.getProperty(H2_DB_FILE_PATH, StringUtils.EMPTY);
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
		this.sfProperties.getEditor().putString(NOTIFICATION_SERVICE, service.name())
				.putString(DEVIICE_TOKEN, deviceToken).apply();

	}

	/**
	 * Enable Chat State Notifications. Which will make typing status visible of
	 * any contact member.
	 * <p>
	 * Please refer XEP-0085.
	 * </p>
	 */
	public void enableChatStateNotification() {
		this.setIsChatStateNotificationEnabled(true);

		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();

		if (chatManager != null) {
			chatManager.setChatStateNotificationEnabled(true);
		}

	}

	/**
	 * Enable Message Delivery Receipt.By enabling it we can see that message is
	 * delivered to contact or not.
	 * <p>
	 * Please refer XEP-0184.
	 * </p>
	 */
	public void enableMessageDeliveryReceipt() {
		this.setIsMessageDeliveryReceiptEnabled(true);
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();

		if (chatManager != null) {
			chatManager.setMessageDeliveryReceiptEnabled(true);
		}
	}

	/**
	 * Enable Chat-Markers. By enabling it we can see message delivery status
	 * and seen unseen by contact.
	 * <p>
	 * Please refer XEP-0333.
	 * </p>
	 * <p>
	 * <p>
	 * This XEP is deferred by xmpp org. But we still support it. If we are not
	 * interested in message sean by receiver and only interested to delivery
	 * status then use {@link #enableMessageDeliveryReceipt()}.
	 * </p>
	 */
	public void enableChatMarkers() {
		this.setIsChatMarkersEnabled(true);
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();

		if (chatManager != null) {
			chatManager.setChatMarkersEnabled(true);
		}
	}

	/**
	 * Enable Media Transfer
	 */
	public void enableMediaTransfer() {
		this.setIsMediaTransferEnabledsEnabled(true);
	}

	/**
	 * Disable Chat State Notifications. Which will make typing status invisible
	 * of any contact member.
	 * <p>
	 * Please refer XEP-0085.
	 * </p>
	 */
	public void disableChatStateNotification() {
		this.setIsChatStateNotificationEnabled(false);
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();

		if (chatManager != null) {
			chatManager.setChatStateNotificationEnabled(false);
		}
	}

	/**
	 * Disable Message Delivery Receipt.By disabling it we insure that we are
	 * not interested in message delivery status.
	 * <p>
	 * Please refer XEP-0184.
	 * </p>
	 */
	public void disableMessageDeliveryReceipt() {
		this.setIsMessageDeliveryReceiptEnabled(false);
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();

		if (chatManager != null) {
			chatManager.setMessageDeliveryReceiptEnabled(false);
		}
	}

	/**
	 * disable Chat-Markers. By disabling it we insure that we are not
	 * interested in message delivery status and seen unseen by contact.
	 * <p>
	 * Please refer XEP-0333.
	 * </p>
	 */
	public void disableChatMarkers() {
		this.setIsChatMarkersEnabled(false);
		AppChatManager chatManager = (AppChatManager) Platform.getInstance().getChatManager();

		if (chatManager != null) {
			chatManager.setChatMarkersEnabled(false);
		}
	}

	/**
	 * disable Media Transfer
	 */
	public void disableMediaTransfer() {
		this.setIsMediaTransferEnabledsEnabled(false);
	}

	/**
	 * Enable Chat State Notifications by setting true. Which will make typing
	 * status visible of any contact member. by default it is false.
	 * <p>
	 * Please refer XEP-0085.
	 * </p>
	 */
	private void setIsChatStateNotificationEnabled(boolean isChatStateNotificationEnabled) {
		this.sfProperties.getEditor().putBoolean(IS_CHAT_STATE_NOTIFICATION_ENABLED, isChatStateNotificationEnabled)
				.apply();
	}

	/**
	 * Enable Message Delivery Receipt.By enabling it we can see that message is
	 * delivered to contact or not. By default this is disabled.
	 * <p>
	 * Please refer XEP-0184.
	 * </p>
	 */
	private void setIsMessageDeliveryReceiptEnabled(boolean isMessageDeliveryReceiptEnabled) {
		this.sfProperties.getEditor().putBoolean(IS_MESSAGE_DELIVERY_RECEIPT_ENABLED, isMessageDeliveryReceiptEnabled)
				.apply();
	}

	/**
	 * Enable Chat-Markers. By enabling it we can see message delivery status
	 * and seen unseen by contact. By default this is disabled.
	 * <p>
	 * Please refer XEP-0333.
	 * </p>
	 * <p>
	 * <p>
	 * This XEP is deferred by xmpp org. But we still support it. If we are not
	 * interested in message sean by receiver and only interested to delivery
	 * status then use {@link #enableMessageDeliveryReceipt()}.
	 * </p>
	 */
	private void setIsChatMarkersEnabled(boolean isChatMarkersEnabled) {
		this.sfProperties.getEditor().putBoolean(IS_CHAT_MARKERS_ENABLED, isChatMarkersEnabled).apply();
	}

	/**
	 * Enable Media Transfer
	 *
	 * @param isMediaTransferEnabledsEnabled
	 */
	private void setIsMediaTransferEnabledsEnabled(boolean isMediaTransferEnabledsEnabled) {
		this.sfProperties.getEditor().putBoolean(IS_MEDIA_TRANSFER_ENABLED, isMediaTransferEnabledsEnabled).apply();
	}

	public boolean isChatStateNotificationEnabled() {
		return this.sfProperties.getBoolean(IS_CHAT_STATE_NOTIFICATION_ENABLED, false);
	}

	public boolean isMessageDeliveryReceiptEnabled() {
		return this.sfProperties.getBoolean(IS_MESSAGE_DELIVERY_RECEIPT_ENABLED, false);
	}

	public boolean isChatMarkersEnabled() {
		return this.sfProperties.getBoolean(IS_CHAT_MARKERS_ENABLED, false);
	}

	public boolean isMediaTransferEnabled() {
		return this.sfProperties.getBoolean(IS_MEDIA_TRANSFER_ENABLED, false);
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
