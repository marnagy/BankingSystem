import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Used as response from server to client in case there already is email
 * signed up with SAME HASHCODE, not same email address
 */
public class EmailAlreadySignedUpResponse extends Response {
	/**
	 * Constructor of EmailAlreadySignedUpResponse object
	 * @param sessionID Long identifier of session
	 */
	public EmailAlreadySignedUpResponse(long sessionID){
		super(ResponseType.EmailAlreadySignedUp, sessionID);
	}

	/**
	 * Method used for sending this object using ObjectOutput
	 * @param oo ObjectOutput to send the object through
	 * @throws IOException Network failure
	 */
	@Override
	void send(ObjectOutput oo) throws IOException {
		oo.writeInt(super.type.ordinal());
		oo.writeLong(sessionID);
		oo.flush();
	}

	/**
	 * Used for loading this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return EmailAlreadySignedUpResponse object
	 * @throws IOException Network failure
	 */
	public static EmailAlreadySignedUpResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new EmailAlreadySignedUpResponse(sessionID);
	}
}
