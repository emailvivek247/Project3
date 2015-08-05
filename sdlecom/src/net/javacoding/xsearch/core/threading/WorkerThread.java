package net.javacoding.xsearch.core.threading;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import net.javacoding.xsearch.core.task.WorkerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of  a Worker Thread.
 * This thread will accept WorkerTasks and execute them.
 *
 */
class WorkerThread extends Thread {

    public static final int WORKERTHREAD_IDLE = 0;
    public static final int WORKERTHREAD_BLOCKED = 1;
    public static final int WORKERTHREAD_BUSY = 2;
    /** the current state of this thread - idle, blocked, or busy. */
    protected int state;

    /** Whether this instance is assigned a task. */
    protected boolean assigned;

    /** Whether we should keep alive this thread. */
    protected boolean running;

    /** Threadpool this worker is part of. */
    protected WorkerThreadPool workerThreadPool;

    /** Task this worker is assigned to. */
    protected WorkerTask task;

    /**
     * Public constructor.
     * @param workerThreadPool thread pool this worker is part of
     * @param name name of the thread
     * @param i index in the pool
     */
    public WorkerThread(WorkerThreadPool workerThreadPool, String name, int i) {
        super(workerThreadPool, name + " " + i);
        this.workerThreadPool = workerThreadPool;
        running = false;
        assigned = false;
        state = WORKERTHREAD_IDLE;
    }

    /**
     * Tests whether this worker thread instance can be assigned a task.
     * @return whether we're capable of handling a task.
     */
    public boolean isAvailable() {
        return (!assigned) && running;
    }

    /**
     * Determines whether we're occupied.
     * @return boolean value representing our occupation
     */
    public boolean isOccupied() {
        return assigned;
    }

    /**
     * Method that allows the threadPool to assign the worker a Task.
     * @param task WorkerTask to be executed.
     */
    public void assign(WorkerTask task) {
        if ( !running ) {
            //SHOULDN'T HAPPEN WITHOUT BUGS
            //return;
            throw new RuntimeException("THREAD NOT RUNNING, CANNOT ASSIGN TASK !!!");
        }
        if (assigned) {
            //SHOULDN'T HAPPEN WITHOUT BUGS
            //return;
            throw new RuntimeException("THREAD ALREADY ASSIGNED !!!");
        }
        this.task = task;
        assigned = true;
    }

    /**
     * Tells this thread not to accept any new tasks.
     */
    public void stopRunning() {
        running = false;
    }

    /**
     * Returns the state of this worker thread (idle, blocked or busy).
     * @return
     */
    public int getCurrentState ( ) {
        return state;
    }

    /**
     * Thread's overridden run method.
     */
    public void run() {
        running = true;

        Logger logger = LoggerFactory.getLogger(this.getClass().getName());
        //logger.debug("Worker thread (" + this.getName() + ") born");

        //notify the pool that this thread has been started fine
        //so that the pool can continue to start other threads
        //see WorkerThreadPool(...) initialization for details
//        synchronized (workerThreadPool) {
//            workerThreadPool.notify();
//        }

        while (running) {
            if (assigned) {
                state = WORKERTHREAD_BLOCKED;
                synchronized (workerThreadPool) {
                    workerThreadPool.notify();
                }
                task.prepare();
                state = WORKERTHREAD_BUSY;
                try {
                    task.execute();
                    task.stop();
                    task.exit();
                } catch (Exception e) {
                    logger.error(NOTIFY_ADMIN,  task + " threw an excpetion!", e);
                    System.exit(1);
                }

                synchronized (workerThreadPool) {
                    assigned = false;
                    task = null;
                    state = WORKERTHREAD_IDLE;
                    //notifiy pool if it's waiting for one thread to assign a task
                    workerThreadPool.notify();
                }
            }else {
                synchronized (workerThreadPool) {
                    try {
                        workerThreadPool.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /* notify the thread pool that we died. */
        //logger.debug("Worker thread (" + this.getName() + ") dying");
    }
}
