import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

public class AccountCreateRequest extends Request {
    public final String email;
    public final char[] passwd;
    public final CurrencyType currency;
    public AccountCreateRequest(String email, char[] passwd, CurrencyType currency,
                                long sessionID){
        super(RequestType.CreateAccount, sessionID);
        this.email = email;
        this.passwd = passwd;
        this.currency = currency;
    }

    @Override
    public void Send(ObjectOutput oo) throws IOException {
        oo.writeInt(super.type.ordinal());
        oo.writeLong(super.sessionID);
        oo.writeUTF(email);
        oo.writeObject(passwd);
        oo.writeInt(currency.ordinal());
        oo.flush();
    }

    public static AccountCreateRequest ReadArgs(ObjectInput oi){
        String email = null;
        char[] passwd = null;
        long sessionID;
        CurrencyType cur;
        try {
            sessionID = oi.readLong();
            email = oi.readUTF();
            passwd = (char[])oi.readObject();
            cur = CurrencyType.values()[oi.readInt()];
            return new AccountCreateRequest(email, passwd, cur, sessionID);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }

    }
}
