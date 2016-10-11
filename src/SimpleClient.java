import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SimpleClient extends Thread {
	private BufferedReader in;
	private PrintWriter out;
	private String cmd;
	private String srvrAddr;

	public SimpleClient(String serverAddress, String command) throws IOException {
		this.srvrAddr = serverAddress;
		this.cmd = command;
	}
	
	public void run() {
		String response;
		try {
			connectToServer(srvrAddr);
			out.println(cmd);
			response = in.readLine();
		} catch (IOException ex) {
			response = "Error: " + ex;
			System.out.println(response + "\n");
		}
	}

	public void connectToServer(String serverAddress) throws IOException {
		Socket socket = new Socket(serverAddress, 9898);
		in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Consume server welcome message
		for (int i = 0; i < 3; i++) {
			in.readLine();
		}
	}

	public static void main(String[] args) {
		int N;
		String serverAddress;
		try {
			N = Integer.parseInt(args[0]);
			serverAddress = args[1];
		} catch (Exception ex) {
			System.out.println("usage: SimpleClient numClients serverAddress");
			return;
		}
		String command = "ADD,1,1";
		for (int i = 0; i < N; i++) {
			try {
				SimpleClient sc = new SimpleClient(serverAddress, command);
				sc.start();
			} catch (Exception ex) {
				System.out.println("client: " + i + " " + ex.toString());
				i--;
			}
		}
	}
}