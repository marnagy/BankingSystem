import java.io.*;
import java.util.regex.Pattern;


public class ClientCLI {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));

        ObjectOutput oo;
        ObjectInput oi;
        Account account = null;
        long sessionID;

        boolean loggedIn = false;

        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        try {
            do {
                ClientSession session = new ClientSession();
                session.connect();
                oo = new ObjectOutputStream(session.getOutputStream());
                oi = new ObjectInputStream(session.getInputStream());
                pw.println("Connected");
                sessionID = ReadSessionID(oi);
                pw.println("Do you want to create an account? [yes / (default) no]");
                pw.flush();
                String respStr;
                RequestType rType;
//            resp = br.readLine();
                respStr = "yes";
                if (respStr.equals("yes")) {
                    rType = RequestType.CreateAccount;
                    String email;
                    boolean success = false;
                    do {
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
                        Request req = new AccountCreateRequest(email, passwd, CurrencyType.EUR, sessionID);
                        // sending
                        req.Send(oo);
                        // receiving
                        ResponseType respType = ResponseType.values()[oi.readInt()];
                        switch (respType) {
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
                        respStr = "yes";
//                        if ((respStr = br.readLine()) == "yes") {
                        if (respStr  == "yes") {
                            break;
                        }
//                    req.ReadArgs(oi);
                    } while (!success);
                }

                pw.println("Enter your email:");
                pw.flush();
//                String email = br.readLine();
                String email = "test@test.cz";
                pw.println("Enter your password:");
                pw.flush();
//                char[] passwd = br.readLine().toCharArray();
                char[] passwd = "test".toCharArray();
                Request req = new LoginRequest(email, passwd, sessionID);
                req.Send(oo);
                // if success, client receives AccountInfo
                ResponseType respType = ResponseType.values()[oi.readInt()];
                Response resp;
                switch (respType) {
                    case IllegalRequestResponse:
                    case AccountInfo:
                        resp = AccountInfoResponse.ReadArgs(oi);
                        if (resp.getClass() == IllegalRequestResponse.class) {
                            continue;
                        }
                        if (resp.getClass() == AccountInfoResponse.class){
                            account = new Account((AccountInfoResponse)resp);
                            loggedIn = true;
                        }
                        break;
                }
            } while (!loggedIn);
            // statement only for breakpoint
            int i = 5;
            // TEST PAYMENT HERE
        } catch (IOException e) {
            System.err.println("IOException occurred");
            e.printStackTrace(System.err);
//        } catch (ArgsException e) {
//            System.err.println("ArgsException occurred");
//            e.printStackTrace();
        } catch (UnknownTypeException e){

        }
    }

    private static long ReadSessionID(ObjectInput oi) throws IOException {
        return oi.readLong();
    }
}
