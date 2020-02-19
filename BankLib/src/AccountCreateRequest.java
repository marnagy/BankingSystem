import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

public class AccountCreateRequest extends Request {
    final RequestType rType = RequestType.CreateAccount;
    public final String email;
    public final char[] passwd;
    public AccountCreateRequest(String email, char[] passwd){
        super(RequestType.CreateAccount);
        this.email = email;
        this.passwd = passwd;
    }

    @Override
    public void Send(ObjectOutput oo) throws IOException {
        oo.writeInt(rType.ordinal());
        oo.writeUTF(email);
        oo.writeUTF(new String(passwd));
        oo.flush();
    }

    public static Request ReadArgs(ObjectInput oi){
        String email = null;
        char[] passwd = null;
        try {
            email = oi.readUTF();
            passwd = oi.readUTF().toCharArray();
            return new AccountCreateRequest(email, passwd);
        } catch (IOException e) {
            return null;
        }

    }
//
//    @Override
//    public void Send(Writer writer) throws IOException {
//        writer.write(rType.toString());
//        writer.write('\n');
//        writer.write(email);
//        writer.write('\n');
//        writer.write(passwd);
//        writer.write('\n');
//        writer.flush();
//    }
}
