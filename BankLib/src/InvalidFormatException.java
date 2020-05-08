/**
 * Exception to throw when needed format is not satisfied
 */
public class InvalidFormatException extends Exception {
	/**
	 * Constructor including messagee
	 * @param msg Message
	 */
	public InvalidFormatException(String msg){
		super(msg);
	}
}
