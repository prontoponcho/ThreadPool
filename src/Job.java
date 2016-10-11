import java.io.PrintWriter;

public class Job {
	private int id;
	private double x;
	private double y;
	private Operation o;
	private String cmd;
	private PrintWriter out;
	
	public Job(int clientID, PrintWriter out, Operation o, double x, double y) {
		this.id = clientID;
		this.out = out;
		this.o = o;
		this.x = x;
		this.y = y;
		cmd = cmd();
	}
	
	private String cmd() {
		StringBuilder sb = new StringBuilder();
		sb.append(o.name());
		sb.append(",");
		sb.append(x);
		sb.append(",");
		sb.append(y);
		return sb.toString();
	}
	
	public void execute() {
		StringBuilder sb = new StringBuilder();
		sb.append(cmd);
		sb.append(" = ");
		try {
			sb.append(rslt());
		} catch (Exception ex) {
			sb.append("null");
		}
		out.println(sb.toString()); //send result to the client
	}
	
	private double rslt() {
		switch (o) {
			case ADD: return x + y;
			case SUB: return x - y;
			case MUL: return x * y;
			case DIV: return x / y;
		}
		return Double.NaN;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("client:");
		sb.append(id);
		sb.append(" job:");
		sb.append(cmd);
		sb.append("=");
		try {
			sb.append(rslt());
		} catch (Exception ex) {
			sb.append("null");
		}
		return  sb.toString();
	}

}
