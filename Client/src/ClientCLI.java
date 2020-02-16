import java.io.IOException;

public class ClientCLI {
    public static void main(String[] args) {
        try {
            ClientSession clSession = new ClientSession();

        } catch (IOException e) {
            System.err.println("IOException occurred");
            e.printStackTrace(System.err);
        }
    }
}
