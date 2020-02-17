import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class Request {
    RequestType type;
    protected Request(RequestType type){
        this.type = type;
    }
    public abstract void Send(ObjectOutput oo) throws IOException;
    public abstract void ReadArgs(ObjectInput oi) throws IOException, ArgsException;
}
