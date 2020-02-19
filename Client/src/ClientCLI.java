import java.io.*;
import java.util.regex.Pattern;

public class ClientCLI {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));

        ObjectOutput oo;
        ObjectInput oi;

        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        try {
            ClientSession session = new ClientSession();
            session.connect();
            oo = new ObjectOutputStream(session.getOutputStream());
            oi = new ObjectInputStream(session.getInputStream());
            pw.println("Connected");
            pw.println("Do you want to create an account? [yes / (default) no]");
            pw.flush();
            String resp;
            RequestType rType;
//            resp = br.readLine();
            resp = "yes";
            if ( resp.equals("yes") ){
                rType = RequestType.CreateAccount;
                String email;
                boolean success = false;
                do{
                    boolean emailValidation = false;
                    do {
                        pw.println("Enter your email:");
                        pw.flush();
                        //email = br.readLine();
                        email = "test@test.cz";
                        emailValidation = emailPattern.matcher(email).matches();
                    } while (!emailValidation);

                    success = false;

                    pw.println("Enter your password:");
                    pw.flush();
                    //char[] passwd = br.readLine().toCharArray();
                    char[] passwd = "test".toCharArray();
                    Request req = new AccountCreateRequest(email, passwd);
                    //sending
                    //Writer writer = new OutputStreamWriter(session.getOutputStream());
                    req.Send(oo);
                    //receiving response
                    //BufferedReader socketbr = new BufferedReader(new InputStreamReader(session.getInputStream()));
                    ResponseType respType = ResponseType.values()[oi.readInt()];
                    switch (respType){
                        case Success:
                            success = true;
                            break;
                        case EmailAlreadySignedUp:
                            pw.println("Email you entered is already signed up.");
                            pw.println("Use different email or sign in using the email.");
                            break;
                        default:
                            throw new UnknownTypeException("Received unknown ResponseType");
                    }
                    pw.println("Do you want to sign in now?");
                    pw.flush();
                    if ( (resp = br.readLine()) == "yes" ){
                        break;
                    }
//                    req.ReadArgs(oi);
                } while (!success);
            }

            pw.println("Enter your email:");
            String email = br.readLine();
            pw.println("Enter your password:");
            String passwd = br.readLine();
        } catch (IOException e) {
            System.err.println("IOException occurred");
            e.printStackTrace(System.err);
//        } catch (ArgsException e) {
//            System.err.println("ArgsException occurred");
//            e.printStackTrace();
        } catch (UnknownTypeException e){

        }
    }
}
