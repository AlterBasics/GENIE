package abs.sf.client.gini.exception;

public class StringflowErrorException extends Exception {
	private static final long serialVersionUID = 4867852581529675552L;

	public StringflowErrorException(String msg, Exception e) {
		super(msg, e);
	}

}
