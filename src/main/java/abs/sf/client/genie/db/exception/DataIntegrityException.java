package abs.sf.client.genie.db.exception;


import java.sql.SQLException;

/**
 * Custom exception for situations where SQL Queries return
 * result sets which are inconsistent to return values.
 */
public class DataIntegrityException extends SQLException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataIntegrityException(String msg){
        super(msg);
    }
}
