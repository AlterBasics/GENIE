package abs.sf.client.genie.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.util.TaskExecutor;
import abs.sf.client.genie.exception.StringflowException;

/**
 * Wrapper around {@link Properties}. For proper handling of properties
 */
public class SFProperties {
	private static final Logger LOGGER = Logger.getLogger(SFProperties.class.getName());

	private String propertiesResource;
	private Properties properties;
	private Editor editor;

	public SFProperties(String propertiesResource) throws StringflowException {
		try {
			this.propertiesResource = propertiesResource;
			this.loadProperties();
			this.showLoadedProperties();
			this.editor = new Editor();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Failed to load properties from file " + propertiesResource, e);
			e.printStackTrace();
			throw new StringflowException("Failed to load properties from file " + propertiesResource, e);
		}

	}

	private void showLoadedProperties() {
		LOGGER.info("Loaded properties from file >> : " + propertiesResource);
		this.properties.list(System.out);
	}

	private void loadProperties() throws IOException {
		this.properties = new Properties();

		try (InputStream is = new FileInputStream(
				getClass().getClassLoader().getResource(this.propertiesResource).getPath())) {
			this.properties.load(is);
		}
	}

	public Editor getEditor() {
		return this.editor;
	}

	/**
	 * Searches for the property with the specified key in this
	 * {@link #properties}. If Key not found then return default value.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getProperty(String key, String defaultValue) {
		String value = this.properties.getProperty(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Searches for the property with the specified key in this
	 * {@link #properties}. If Key not found then return default value.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getInt(String key, int defaultValue) {
		String value = this.properties.getProperty(key);

		if (value != null) {
			return Integer.parseInt(value);
		}

		return defaultValue;
	}

	/**
	 * Searches for the property with the specified key in this
	 * {@link #properties}. If Key not found then return default value.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public long getLong(String key, long defaultValue) {
		String value = this.properties.getProperty(key);

		if (value != null) {
			return Long.parseLong(value);
		}

		return defaultValue;
	}

	/**
	 * Searches for the property with the specified key in this
	 * {@link #properties}. If Key not found then return default value.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public float getFloat(String key, float defaultValue) {
		String value = this.properties.getProperty(key);

		if (value != null) {
			return Float.parseFloat(value);
		}

		return defaultValue;
	}

	/**
	 * Searches for the property with the specified key in this
	 * {@link #properties}. If Key not found then return default value.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		String value = this.properties.getProperty(key);

		if (value != null) {
			return Boolean.parseBoolean(value);
		}

		return defaultValue;
	}

	/**
	 * Searches for the property with the specified key in this
	 * {@link #properties}. If Key not found then return default value.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public boolean contains(String key) {
		return this.properties.containsKey(key);
	}

	public class Editor {

		private Editor() {

		}

		/**
		 * It will add key-value payer in memory map. To persist changes on disk
		 * use {@link #commit()} or {@link #apply()}.
		 * 
		 * @param key
		 * @param value
		 *
		 */
		public Editor putString(String key, String value) {
			properties.setProperty(key, value);
			return this;
		}

		/**
		 * It will add key-value payer in memory map. To persist changes on disk
		 * use {@link #commit()} or {@link #apply()}.
		 * 
		 * @param key
		 * @param value
		 */
		public Editor putInt(String key, int value) {
			properties.setProperty(key, Integer.toString(value));
			return this;
		}

		/**
		 * It will add key-value payer in memory map. To persist changes on disk
		 * use {@link #commit()} or {@link #apply()}.
		 * 
		 * @param key
		 * @param value
		 */
		public Editor putLong(String key, long value) {
			properties.setProperty(key, Long.toString(value));
			return this;
		}

		/**
		 * It will add key-value payer in memory map. To persist changes on disk
		 * use {@link #commit()} or {@link #apply()}.
		 * 
		 * @param key
		 * @param value
		 */
		public Editor putFloat(String key, float value) {
			properties.setProperty(key, Float.toString(value));
			return this;
		}

		/**
		 * It will add key-value payer in memory map. To persist changes on disk
		 * use {@link #commit()} or {@link #apply()}.
		 * 
		 * @param key
		 * @param value
		 */
		public Editor putBoolean(String key, boolean value) {
			properties.setProperty(key, Boolean.toString(value));
			return this;
		}

		/**
		 * It will remove property from memory. To persist changes on disk use
		 * {@link #commit()} or {@link #apply()}.
		 * 
		 * @param key
		 */
		public Editor remove(String key) {
			properties.remove(key);
			return this;
		}

		/***
		 * It will clear all data In-memory. After it to clear on disk you can
		 * use {@link #commit()} or {@link #apply()}.
		 * 
		 */
		public Editor clear() {
			properties.clear();
			return this;
		}

		/**
		 * It starts an asynchronous commit to disk and you will be notified for
		 * any failures.
		 * 
		 * @throws StringflowException
		 */
		public void commit() throws StringflowException {
			LOGGER.info("COMMITING PROPERTIES for file >>>>>>>>>>>>>>> : " + propertiesResource);
			try (OutputStream output = new FileOutputStream(
					getClass().getClassLoader().getResource(propertiesResource).getPath())) {

				properties.store(output, null);
				LOGGER.info("COMMITED PROPERTIES IN file >>>>>>>>>>>>>>> : " + propertiesResource);
				properties.list(System.out);

			} catch (IOException e) {
				LOGGER.log(Level.INFO, "Failed to save properties in property file : " + propertiesResource, e);
				e.printStackTrace();
				throw new StringflowException("Failed to save properties in property file : " + propertiesResource,
						e);
			}
		}

		/**
		 * It starts an asynchronous commit to disk and you won't be notified of
		 * any failures.
		 * 
		 */
		public void apply() {
			TaskExecutor.submit(() -> {
				try {
					commit();

				} catch (StringflowException e) {
					LOGGER.log(Level.INFO, "Failed to save properties in property file : " + propertiesResource, e);
				}
			});
		}
	}
}
