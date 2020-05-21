/**
 * Exception to throw when needed format is not satisfied
 */
public class InvalidFormatException extends Exception {
	/**
	 * Creates object including message
	 * @param msg Message
	 */
	public InvalidFormatException(String msg){
		super(msg);
	}
}
