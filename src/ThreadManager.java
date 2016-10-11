
public class ThreadManager extends Thread {
	private ThreadPool pool;
	private SharedQueue shdq;
	private int V;				//sleep time in milliseconds
	private int T1;				//lower threshold
	private int T2;				//upper threshold
	private int qsize;		    //last known number of jobs
	
	public ThreadManager(ThreadPool t, SharedQueue q, int V, int T1, int T2) {
		this.pool = t;
		this.shdq = q;
		this.V = V * 1000; 		//V argument converted to milliseconds.
		this.T1 = T1;
		this.T2 = T2;
	}
	
	public void run() {
		pool.startPool();
		while (!interrupted()) {
			try {
				int size = shdq.size();
				int delta = size - qsize;
				if (thresholdCrossed(delta)) {
					if (delta < 0) {
						pool.halvePoolSize();
						Server.log("ThreadManager halved pool size to " + pool.numThreads());
					} else {
						pool.doublePoolSize();
						Server.log("ThreadManager doubled pool size to " + pool.numThreads());
					}
				}
				qsize = size;
			} catch (Exception ex) {
				//do nothing
			}
		}
		pool.stopPool();
	}
	
	private boolean thresholdCrossed(int delta) {
		if (qsize <= T2 && (qsize + delta) > T2)
			return true;
		if (qsize <= T1 && (qsize + delta) > T1)
			return true;
		if (qsize > T2 && (qsize + delta) <= T2)
			return true;
		if (qsize > T1 && (qsize + delta) <= T1)
			return true;
		return false;
	}

}
