package tsdb.explorer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal Executor for threads in TimeSeriesMultiViewScene
 * @author woellauer
 *
 */
public class PriorityExecutor {
	
	private ThreadPoolExecutor executor;
	private PriorityBlockingQueue<Runnable> queue;
	
	private static class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
		final Runnable entry;
		final int seqNum;
		final int priority;
		public PriorityRunnable(Runnable entry,int seqNum,int priority) {
			this.entry = entry;
			this.seqNum = seqNum;
			this.priority = priority;
		}
		@Override
		public int compareTo(PriorityRunnable o) {
			if(this.priority<o.priority) {
				return -1;
			}
			if(this.priority>o.priority) {
				return +1;
			}
			return this.seqNum < o.seqNum ? -1 : 1;
		}
		@Override
		public void run() {
			entry.run();			
		}
	}
	
	static class PriorityThreadFactory implements ThreadFactory { // source from Executors.defaultThreadFactory();
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int priority;

		PriorityThreadFactory(int priority) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
				Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" +
					poolNumber.getAndIncrement() +
					"-thread-";
			this.priority = priority;
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
					namePrefix + threadNumber.getAndIncrement(),
					0);
			if (t.isDaemon())
				t.setDaemon(false);
			/*if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);*/
			t.setPriority(priority);
			return t;
		}
	}

	static ThreadPoolExecutor createPriorityExecutor(int nThreads, int priority) {
		int corePoolSize = nThreads;
		int maximumPoolSize = nThreads;
		long keepAliveTime = 0L;
		TimeUnit unit = TimeUnit.MILLISECONDS;
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
		//ThreadFactory threadFactory = Executors.defaultThreadFactory();
		ThreadFactory threadFactory = new PriorityThreadFactory(priority);
		/*ThreadFactory threadFactory = new ThreadFactory() {			
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setPriority(Thread.MIN_PRIORITY);
				return null;
			}
		};*/
		RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
		return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, rejectedExecutionHandler);

	}

	public PriorityExecutor() {
		queue = new PriorityBlockingQueue<Runnable>();
		//final int nThreads = ForkJoinPool.getCommonPoolParallelism();
		//final int nThreads = 16;
		final int nThreads = 1;
		System.out.println("nThreads "+nThreads);
		executor = new ThreadPoolExecutor(nThreads, nThreads, 100, TimeUnit.MILLISECONDS, queue);
	}
	
	public void addTask(Runnable entry,int seqNum,int priority) {
		executor.execute(new PriorityRunnable(entry,seqNum,priority));
	}
	
	public void clear() {
		queue.clear();
	}

}
