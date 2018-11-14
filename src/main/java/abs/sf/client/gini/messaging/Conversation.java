package abs.sf.client.gini.messaging;

import abs.ixi.client.util.DateUtils;
import abs.sf.client.gini.messaging.ChatLine.ContentType;

public class Conversation {
    private String peerJid;
    private String peerName;
    private String lastChatLine;
    private ContentType lastChatLineType;
    private String displayTime;
    private long lastUpdateTime;
    private int unreadChatLines;
    private boolean isGroup;
    private boolean online;
    private boolean isTyping;

    public Conversation(String peerJid, String peerName, boolean isGroup) {
        this.peerJid = peerJid;
        this.peerName = peerName;
        this.isGroup = isGroup;
        this.lastUpdateTime = DateUtils.currentTimeInMiles();
        this.displayTime = DateUtils.getDisplayTime(DateUtils.currentTimeInMiles());
    }

    public Conversation(ChatLine chatLine) {
        this.peerJid = chatLine.getPeerBareJid();
        this.lastChatLine = chatLine.getText();
        this.lastChatLineType = chatLine.getContentType();
        this.displayTime = chatLine.getDisplayTime();
        this.lastUpdateTime = DateUtils.currentTimeInMiles();
    }

    public String getPeerJid() {
        return peerJid;
    }

    public void setPeerJid(String peerJid) {
        this.peerJid = peerJid;
    }

    public String getPeerName() {
        return peerName;
    }

    public int getUnreadChatLines() {
        return unreadChatLines;
    }

    public void setUnreadChatLines(int unreadChatLines) {
        this.unreadChatLines = unreadChatLines;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getLastChatLine() {
        return lastChatLine;
    }

    public void setLastChatLine(String lastChatLine) {
        this.lastChatLine = lastChatLine;
    }

    public ContentType getLastChatLineType() {
        return lastChatLineType;
    }

    public void setLastChatLineType(ContentType lastChatLineType) {
        this.lastChatLineType = lastChatLineType;
    }

    public void setLastChatLineType(String type) {
        try {
            this.lastChatLineType = ContentType.valueOf(type);
        } catch (Exception e) {
            //swallow exception
            this.lastChatLineType = ContentType.TEXT;
        }
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isTyping() {
        return isTyping;
    }
}
