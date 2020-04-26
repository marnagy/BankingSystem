public class Main {
    public static void main(String[] args) {
    	var master = MasterServerSession.getDefault();
    	master.run(args[0], args[1].toCharArray());
		//MasterServerSession.Run(args[0], args[1].toCharArray());
    }
}
