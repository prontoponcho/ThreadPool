import java.util.LinkedList;

public class SharedQueue {
	private LinkedList<Job> q;
	private final int MAXCAP = 50;
	
	public SharedQueue() {
		q = new LinkedList<Job>();
	}
	
	public synchronized boolean enqueue(Job j) {
		if (q.size() >= MAXCAP) {
			return false;
		}
		q.add(j);
		notifyAll();
		return true;
	}
	
	public synchronized Job dequeue() {
		while (q.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}	
		}
		Job j = q.removeFirst();
		notifyAll();
		return j;
	}
	
	public int size() {
		return q.size();
	}

}
