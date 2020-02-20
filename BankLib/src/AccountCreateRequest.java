import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

public class AccountCreateRequest extends Request {
    final RequestType rType = RequestType.CreateAccount;
    public final String email;
    public final char[] passwd;
    public final Currency currency;
    public AccountCreateRequest(String email, char[] passwd, Currency currency){
        super(RequestType.CreateAccount);
        this.email = email;
        this.passwd = passwd;
        this.currency = currency;
    }

    @Override
    public void Send(ObjectOutput oo) throws IOException {
        oo.writeInt(rType.ordinal());
        oo.writeUTF(email);
        oo.writeObject(passwd);
        oo.writeInt(currency.ordinal());
        oo.flush();
    }

    public static Request ReadArgs(ObjectInput oi){
        String email = null;
        char[] passwd = null;
        Currency cur;
        try {
            email = oi.readUTF();
            passwd = (char[])oi.readObject();
            cur = Currency.values()[oi.readInt()];
            return new AccountCreateRequest(email, passwd, cur);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }

    }
}
