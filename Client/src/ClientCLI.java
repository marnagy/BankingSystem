import java.io.*;
import java.util.regex.Pattern;

public class ClientCLI {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));

        ObjectOutput oo;
        ObjectInput oi;

        Pattern emailPattern = Pattern.compile("^[\\\\w!#$%&’*+/=?`{|}~^-]+(?:\\\\.[\\\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,6}$");
        try {
            ClientSession session = new ClientSession();
            session.connect();
            oo = new ObjectOutputStream(session.getOutputStream());
            oi = new ObjectInputStream(session.getInputStream());
            pw.println("Connected");
            pw.println("Do you want to create an account? [yes / (default) no]");
            String resp;
            RequestType rType;
            if ( (resp = br.readLine()) == "yes" ){
                rType = RequestType.CreateAccount;
                String email;
                do {
                    pw.println("Enter your email:");
                    email = br.readLine();
                } while (!emailPattern.matcher(email).matches());
                pw.println("Enter your password:");
                char[] passwd = br.readLine().toCharArray();
                Request req = new AccountCreateRequest(email, passwd);
                //sending
                req.Send(oo);
                //receiving response
                req.ReadArgs(oi);
            }

            pw.println("Enter your email:");
            String email = br.readLine();
            pw.println("Enter your password:");
            String passwd = br.readLine();
        } catch (IOException e) {
            System.err.println("IOException occurred");
            e.printStackTrace(System.err);
        } catch (ArgsException e) {
            System.err.println("ArgsException occurred");
            e.printStackTrace();
        }
    }
}
