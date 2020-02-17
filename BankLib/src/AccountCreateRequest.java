import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class AccountCreateRequest extends Request {
    final RequestType rType = RequestType.CreateAccount;
    String email;
    char[] passwd;
    public AccountCreateRequest(String email, char[] passwd){
        super(RequestType.CreateAccount);
        this.email = email;
        this.passwd = passwd;
    }

    @Override
    public void Send(ObjectOutput oo) throws IOException {
        oo.writeUTF(rType.toString());
        oo.writeUTF(email);
        oo.writeUTF(new String(passwd));
    }

    @Override
    public void ReadArgs(ObjectInput oi) throws IOException, ArgsException {
        ResponseType type;
        String respTypeStr = oi.readUTF();
        type = ResponseType.valueOf(respTypeStr);
        switch (type){
            case Successful:
                return;
            default:
                throw new ArgsException("Received response type " + type);
        }
    }
}
