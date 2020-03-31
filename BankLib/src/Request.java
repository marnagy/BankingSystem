import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

public abstract class Request {
    public final RequestType type;
    public final long sessionID;
    protected Request(RequestType type, long sessionID){
        this.type = type;
        this.sessionID = sessionID;
    }
    // used by client
    public abstract void Send(ObjectOutput oo) throws IOException;
    // used by server
    public static Request ReadArgs(ObjectInput oi) throws IOException, ClassNotFoundException {
        assert false;
        return null;
    }
}
