package net.javacoding.xsearch.core.task;

/**
 * Interface that will be implemented upon each class that represents a JFetcher
 * workertask that needs to be executed by a Worker Thread. JFetcher has two
 * types of tasks: spider tasks (that fetch data from a web server), and writer
 * tasks (that interpret data, take decision, etc...)
 */
public interface WorkerTask extends Task {

    /**
     * Task type that is used for every task that will require the fetching of
     * data from a site.
     */
    public static final int WORKERTASK_RETRIEVERTASK = 1;

    /**
     * Task type used for all tasks that don't require any fetching of data
     */
    public static final int WORKERTASK_WRITERTASK    = 2;

    public int getContextId();

    /**
     * Returns the type of the task - spider or writer.
     * 
     * @return the type of the task
     */
    public int getType();

    /**
     * Allows some work to be done before the actual Task is carried out. During
     * the invocation of prepare, the WorkerThread's state will be
     * WORKERTHREAD_BLOCKED.
     */
    public void prepare();

    /**
     * This exit() should only be called by WorkerThread
     */
    public void exit();

    public void stop();

    public int getTotalDocumentsDone();

}
