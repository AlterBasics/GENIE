package abs.sf.client.gini.messaging;

import abs.ixi.client.xmpp.packet.Presence.PresenceStatus;

/**
 * {@code UserPresence} captures a row from {@link PresenceTable}
 */
public class UserPresence {
	private String jid;
	private boolean online;
	private String mood;
	private PresenceStatus status;
	private String lastUpdateTime;

	public UserPresence(String jid) {
		this(jid, false);
	}

	public UserPresence(String jid, boolean online) {
		this.jid = jid;
		this.online = online;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

	public PresenceStatus getStatus() {
		return status;
	}

	public void setStatus(PresenceStatus status) {
		this.status = status;
	}

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
}
