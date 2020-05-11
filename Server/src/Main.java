import java.util.regex.Pattern;

/**
 * Main class for starting server
 */
public class Main {
    public static void main(String[] args) {
    	if (args.length == 2 &&
			    Pattern.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",args[0])
    		&& args[1] != ""){
		    var master = MasterServerSession.getDefault();
		    master.run(args[0], args[1].toCharArray());
	    }
    	else{
    		System.out.println("Invalid arguments received.");
	    }
    }
}
