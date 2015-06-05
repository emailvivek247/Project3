package net.javacoding.xsearch.core.threading;

import net.javacoding.xsearch.core.task.DispatcherTask;
import net.javacoding.xsearch.core.task.WorkerTask;

/**
 * Thread Pool implementation that will be used for pooling the spider and
 * parser threads.
 */
public class WorkerThreadPool extends ThreadGroup {

    /** Task Dispatcher thread associated with this threadpool. */
    protected DispatcherThread dispatcherThread;

    /** Array of threads in the pool. */
    protected WorkerThread[] threads;

    /** Size of the pool. */
    protected int poolSize;
    
    /** running or not **/
    protected boolean running;

    /**
     * Public constructor
     * @param poolName name of the threadPool
     * @param threadName name for the worker Threads
     * @param poolSize number of threads in the pool
     */
    public WorkerThreadPool(String poolName, String threadName, int poolSize) {
        super(poolName);

        this.poolSize = poolSize;

        dispatcherThread = new DispatcherThread(this, threadName + " dispatcher", this);
        threads = new WorkerThread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            threads[i] = new WorkerThread(this, threadName, i);
            threads[i].start();
        }
        running = true;
    }

    /**
     * Assigns a worker task to the pool.  The threadPool will select a worker
     * thread to execute the task.
     * @param task the WorkerTask to be executed.
     */
    public synchronized void assign(WorkerTask task) {
        while (running) {
            for (int i = 0; i < poolSize; i++) {
                if (threads[i].isAvailable()) {
                    threads[i].assign(task);
                    this.notifyAll();
                    return;
                }
            }
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Assigns a DispatcherTask to the threadPool.  The dispatcher thread
     * associated with the threadpool will execute it.
     * @param task DispatcherTask that will keep the workers busy
     */
    public void assignGroupTask(DispatcherTask task) {
        dispatcherThread.assign(task);
    }

    /**
     * Returns the percentage of worker threads that are busy.
     * @return int value representing the percentage of busy workers
     */
    public int getOccupation() {
        int occupied = 0;
        for (int i = 0; i < poolSize; i++) {
            WorkerThread thread = threads[i];
            if (thread.isOccupied()) {
                occupied++;
            }
        }
        return (occupied * 100) / poolSize;
    }

    public int getBlockedPercentage() {
        int counter = 0;
        for (int i = 0; i < poolSize; i++) {
            WorkerThread thread = threads[i];
            if (thread.getCurrentState() == WorkerThread.WORKERTHREAD_BLOCKED ) {
                counter++;
            }
        }
        return (counter * 100) / poolSize;
    }

    public int getBusyPercentage () {
        int counter = 0;
        for (int i = 0; i < poolSize; i++) {
            WorkerThread thread = threads[i];
            if (thread.getCurrentState() == WorkerThread.WORKERTHREAD_BUSY) {
                counter++;
            }
        }
        return (counter * 100) / poolSize;
    }

    public int getIdlePercentage ( ) {
        int counter = 0;
        for (int i = 0; i < poolSize; i++) {
            WorkerThread thread = threads[i];
            if (thread.getCurrentState() == WorkerThread.WORKERTHREAD_IDLE ) {
                counter++;
            }
        }
        return (counter * 100) / poolSize;
    }

    /**
     * Causes all worker threads to die.
     */
    public void stopAll() {
        running = false;
        for (int i = 0; i < threads.length; i++) {
            WorkerThread thread = threads[i];
            thread.stopRunning();
        }
    }

    /**
     * Returns the number of worker threads that are in the pool.
     * @return the number of worker threads in the pool
     */
    public int getSize ( ) {
        return poolSize;
    }

}
