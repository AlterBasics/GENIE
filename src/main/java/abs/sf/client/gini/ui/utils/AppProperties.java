package abs.sf.client.gini.ui.utils;

import abs.ixi.client.util.StringUtils;
import abs.sf.client.gini.exception.StringflowErrorException;
import abs.sf.client.gini.utils.SFProperties;

public class AppProperties {
	private static final String APP_PROPERTIES_RESOURCE = "conf/app.properties";

	private SFProperties properties;
	private static AppProperties instance;

	/**
	 * Restricting access to local
	 * 
	 * @throws StringflowErrorException
	 * 
	 */
	private AppProperties() throws StringflowErrorException {
		this.properties = new SFProperties(APP_PROPERTIES_RESOURCE);
	}

	/**
	 * Returns the singleton instance of {@code GINIAppProperties}
	 * 
	 * @throws StringflowErrorException
	 */
	public static AppProperties getInstance() throws StringflowErrorException {
		if (instance == null) {
			instance = new AppProperties();
		}

		return instance;
	}

	public String get(String key) {
		return this.properties.getProperty(key, StringUtils.EMPTY);
	}

	public void remove(String key) {
		this.properties.getEditor().remove(key).apply();
	}

	/**
	 * Removes all the preferences from the object it is editing.
	 */
	public void clear() {
		this.properties.getEditor().clear().apply();
	}

	public void setUsername(String val) {
		this.properties.getEditor().putString(AppPropertiesName.USER_NAME, val).apply();
	}

	public String getUsername() {
		return this.properties.getProperty(AppPropertiesName.USER_NAME, StringUtils.EMPTY);
	}

	public void setPassword(String val) {
		this.properties.getEditor().putString(AppPropertiesName.PASSWORD, val).apply();
	}

	public String getPassword() {
		return this.properties.getProperty(AppPropertiesName.PASSWORD, StringUtils.EMPTY);
	}

	public void setLoginStatus(boolean status) {
		this.properties.getEditor().putBoolean(AppPropertiesName.LOGIN_STATUS, status).apply();
	}

	public boolean isPreviouslyLoggedin() {
		return this.properties.getBoolean(AppPropertiesName.LOGIN_STATUS, false);
	}

	public String getXMPPServerIP() {
		return this.properties.getProperty(AppPropertiesName.XMPP_SERVER_IP, StringUtils.EMPTY);
	}

	public int getXMPPServerPort() {
		return this.properties.getInt(AppPropertiesName.XMPP_SERVER_PORT, 0);
	}

	public String getMediaServerIP() {
		return this.properties.getProperty(AppPropertiesName.MEDIA_SERVER_IP, StringUtils.EMPTY);
	}

	public int getMediaServerPort() {
		return this.properties.getInt(AppPropertiesName.MEDIA_SERVER_PORT, 0);
	}

	public String getDomainName() {
		return this.properties.getProperty(AppPropertiesName.DOMAIN_NAME, StringUtils.EMPTY);
	}

	public String getApplicationName() {
		return this.properties.getProperty(AppPropertiesName.APPLICATION_NAME,
				AppPropertiesName.APPLICATION_DEFAULT_NAME);
	}

	public static void main(String args[]) {
		System.out.println(getInstance().getXMPPServerIP());

	}
}
