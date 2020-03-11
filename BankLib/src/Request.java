import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

public abstract class Request {
    RequestType type;
    protected Request(RequestType type){
        this.type = type;
    }
    // used by client
    public abstract void Send(ObjectOutput oo) throws IOException;
    // used by server
    public static Request ReadArgs(ObjectInput oi) throws IOException, ArgsException, ClassNotFoundException {
        assert false;
        return null;
    }
}
