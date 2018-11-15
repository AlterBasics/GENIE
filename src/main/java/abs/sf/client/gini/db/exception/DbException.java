package abs.sf.client.gini	.db.exception;

/**
 * Captures database related exception for application layer.
 * Ideally it should not be used inside core db layer. This
 * should be used as a wrapper exception for application layer.
 */
public class DbException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = 7342830770560109770L;

	public DbException(){
        super();
        //do-nothing constructor
    }

    public DbException(String msg){
        super(msg);
    }
    
    public DbException(String msg, Exception e){
        super(msg, e);
    }

    public DbException(Exception cause){
        super(cause);
    }
}
