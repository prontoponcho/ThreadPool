
public class ThreadPool {
	private final int MAXCAP = 50;				//maximum number of threads in the pool
	private final int MINCAP = 5;				//minimum number of threads in the pool
	private int N = 0;							//current number of threads
	private Thread pool[] = new Thread[MAXCAP]; //stores the worker thread references
	private SharedQueue shdq; 					//shared by Workers, ThreadManager, and Server.
	
	class Worker implements Runnable {
		private int id;
		
		Worker(int id) { this.id = id; }
		
		public void run() {
			while (!Thread.interrupted()) {
				try { Thread.sleep(20); } 		//for artificially causing pool resizing
				catch (Exception e) { /*do nothing*/ }
				Job j = shdq.dequeue();
				j.execute();
				Server.log("Worker id=" + id + " processed " + j.toString());
			}
		}
		
	}
	
	public ThreadPool(SharedQueue qm) {
		if (qm == null)
			throw new IllegalArgumentException("shared queue monitor cannot be null");
		for (int i = 0; i < MINCAP; i++)
			pool[i] = new Thread(new Worker(i));
		this.N = MINCAP;
		this.shdq = qm;
	}
	
	//start all available threads in the pool
	public synchronized void startPool() {
		for (int i = 0; i < N; i++) 
			pool[i].start();
	}
	
	//double the threads in pool
	public void doublePoolSize() {
		if (N == MAXCAP)
			throw new RuntimeException("number of threads cannot exceed MAXCAP");
		int size = N * 2;
		if (size > MAXCAP) size = MAXCAP;
		for (int i = N; i < size; i++) {
			pool[i] = new Thread(new Worker(i));
			pool[i].start();
		}
		N = size;
	}
	
	//halve the threads in pool
	public void halvePoolSize() {
		if (N == MINCAP)
			throw new RuntimeException("cannot halve pool size 5");
		int size = N / 2;
		if (size < MINCAP) size = MINCAP;
		for (int i = N; i >= size;) {
			pool[--i].interrupt();
			pool[i] = null;
		}
		N = size;
	}
	
	//terminate all threads in the pool
	public synchronized void stopPool() {
		for (int i = 0; i < MAXCAP; i++) {
			if (pool[i] != null) {
				pool[i].interrupt();
				pool[i] = null;
			}
		}
	}
	
	public int numThreads() {
		return N;
	}
	
	public int maxCapacity() {
		return MAXCAP;
	}

	public int minCapacity() {
		return MINCAP;
	}

}
