import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Sends response in case there already is email
 * signed up with SAME HASHCODE, not same email address
 */
public class EmailAlreadySignedUpResponse extends Response {
	/**
	 * Creates an EmailAlreadySignedUpResponse object
	 * @param sessionID Long identifier of session
	 */
	public EmailAlreadySignedUpResponse(long sessionID){
		super(ResponseType.EmailAlreadySignedUp, sessionID);
	}

	/**
	 * Sends this object using ObjectOutput
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
	 * Loads this object from ObjectInput
	 * @param oi ObjectInput object
	 * @return EmailAlreadySignedUpResponse object
	 * @throws IOException Network failure
	 */
	public static EmailAlreadySignedUpResponse readArgs(ObjectInput oi) throws IOException {
		long sessionID = oi.readLong();
		return new EmailAlreadySignedUpResponse(sessionID);
	}
}
