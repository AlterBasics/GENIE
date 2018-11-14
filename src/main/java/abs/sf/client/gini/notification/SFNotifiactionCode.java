package abs.sf.client.gini.notification;

public enum SFNotifiactionCode {
	RE_CONNECT(1000),
	TEXT_MESSAGE(1001),
	MEDIA_MESSAGE(1002);
	
	public static final String SF_NOTIFICATION_CODE = "SF_NOTIFICATION_CODE";
	
	int val;
	
	private SFNotifiactionCode(int val) {
		this.val = val;
	}

	public int val() {
		return val;
	}

	public static SFNotifiactionCode valueFrom(int val) throws IllegalArgumentException {
		for (SFNotifiactionCode type : values()) {
			if (type.val() == val) {
				return type;
			}
		}

		throw new IllegalArgumentException("No SFNotifiactionCode for value [" + val + "]");
	}
}
