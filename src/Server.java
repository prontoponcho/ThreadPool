import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Server extends Thread {
	private static int N; 								//count of all successful connections
	private final int MAXLOAD = 200; 					//max number of active clients allowed
	private static SharedQueue shdq;					//queue for holding client jobs
	private static HashMap<Integer, Connection> cnxs;	//active connections
	private static ThreadManager mngr;					//manages thread pool
	private static ServerSocket listener;
	
	public Server() { }
	
	public void run() {
		System.out.println("The server is running.");
		N = 0;
		shdq = new SharedQueue();
		cnxs = new HashMap<Integer, Connection>();
		ThreadPool pool = new ThreadPool(shdq);
		mngr = new ThreadManager(pool, shdq, 0, 10, 20);
		
		try { listener = new ServerSocket(9898); } 
		catch (IOException e) { log("Server failed to create ServerSocket"); }
		
		try {
			mngr.start();
			while (true) {
				Socket clientSocket = listener.accept();
				if (cnxs.size() < MAXLOAD) {
					Connection c = new Connection(clientSocket, N, shdq);
					cnxs.put(N, c);
					log("New connection with client# " + N + " at " + clientSocket);
					N++;
					c.start();
				} else {
					log("Server load exceeded");
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					out.println("The server is currently busy. Try reconnecting later.");
					clientSocket.close();
				}
			}
		} catch (IOException e) {
			//do nothing
		} finally {
			try { listener.close(); } 
			catch (IOException e) { 
				//do nothing
			}
		}
	}//Server.run()
   
	/**Connection listens for client commands, 
	 * parses the commands to create a job, 
	 * and stores jobs in the shared queue.
	 */
    private static class Connection extends Thread {
        private Socket socket; 			//connection to client
        private int id;					//client id
        private SharedQueue shdq;		//shared queue stores jobs created by all connections	

        public Connection(Socket socket, int clientNumber, SharedQueue q) {
            this.socket = socket;
            this.id = clientNumber;
            this.shdq = q;
        }

        public void run() {
            try {
            	//initialize streams
                BufferedReader in = new BufferedReader(
                		new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + id + ".");
                out.println("Usage: OPERAND,INT,INT or KILL to quit\n");

                //listen for commands and create jobs
                while (true) {
                	String line = in.readLine();
                	if (line == null) continue;
                	String[] cmd = line.split(",");
                    if (cmd.length == 1 && cmd[0].equals("KILL")) {
                    	Server.log("KILL command received from client# " + id);
                    	break;
                    } else if (cmd.length != 3) {
                    	out.println("Command \"" + line + "\" not recognized. Usage: OPERAND,INT,INT");
                    } else {
                    	Job j = createJob(id, out, cmd);
                    	if (j == null)
                    		out.println("Command \"" + line + "\" not recognized. Usage: OPERAND,INT,INT");
                    	else {
                    		if (!shdq.enqueue(j))
                    			out.println("The server is currently busy. Try resending the command.");
                    	}
                    }
                }
                
            } catch (IOException e) {
                Server.log("Error handling client# " + id + ": " + e);
            } finally {
                close();
                cnxs.remove(id);
            }
            killServer();
        }//Connection.run()
        
        private void close() {
        	try { socket.close(); } 
        	catch (IOException e) { 
        		Server.log("Failed to close socket for client: " + id);
        	}
        }
        
    }//Connection.class
    
    //parses client commands to create a job for execution
    private static Job createJob(int id, PrintWriter out, String[] cmd) {
        try {
            int x = Integer.parseInt(cmd[1]);
            int y = Integer.parseInt(cmd[2]);
            switch (cmd[0]) {
                case "ADD": 
                	return new Job(id, out, Operation.ADD, x, y);
                case "SUB": 
                	return  new Job(id, out, Operation.SUB, x, y);
                case "MUL": 
                	return  new Job(id, out, Operation.MUL, x, y);
                case "DIV": 
                	return  new Job(id, out, Operation.DIV, x, y);
            }
        } catch (Exception ex) {
        	//do nothing
        }
        return null;
    }
    
    //closes all client sockets and then server socket
    private static void killServer() {
    	log("Killing client connections");
    	for (Integer id : cnxs.keySet()) {
    		cnxs.get(id).close();
    		cnxs.remove(id);
    	}
    	log("Killing thread manager");
    	mngr.interrupt();
    	log("Killing server");
    	try { listener.close(); } 
    	catch (IOException e) {
    		log("Failed to close server socket");
		} finally {
			System.exit(0);
		}
    }
    
    public static void log(String message) {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        System.out.println(message + " at " + timeStamp);
    }
    
    public static void main(String[] args) {
    	try {
			Server s = new Server();
			s.start();
		} catch (Exception e) {
			System.out.println("Server failed with exception: " + e.getMessage());
		}
    }
}
