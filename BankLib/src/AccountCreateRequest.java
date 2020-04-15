import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

public class AccountCreateRequest extends Request {
    public final String email;
    public final char[] passwd;
    public AccountCreateRequest(String email, char[] passwd, long sessionID){
        super(RequestType.CreateAccount, sessionID);
        this.email = email;
        this.passwd = passwd;
    }

    @Override
    public void send(ObjectOutput oo) throws IOException {
        oo.writeInt(super.type.ordinal());
        oo.writeLong(super.sessionID);
        oo.writeUTF(email);
        oo.writeObject(passwd);
        oo.flush();
    }

    public static AccountCreateRequest readArgs(ObjectInput oi){
        try {
            long sessionID = oi.readLong();
            String email = oi.readUTF();
            char[] passwd = (char[])oi.readObject();
            return new AccountCreateRequest(email, passwd, sessionID);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }

    }
}
