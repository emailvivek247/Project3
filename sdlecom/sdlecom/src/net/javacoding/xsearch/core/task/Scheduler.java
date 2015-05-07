package net.javacoding.xsearch.core.task;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.TaskAssignmentException;
import net.javacoding.xsearch.core.task.impl.SchedulerImpl.TaskContext;


/**
 * Interface that will be implemented upon each object that will act as a task
 * scheduler.
 * The Task scheduler will keep track of all work that is being done and all
 * tasks that still have to be carried out.
 * Based on code from @author  G� Van Roey
 * 
 * 
 */
public interface Scheduler {

    public IndexerContext getIndexerContext( ) ;
    /**
     * Schedules a Worker Task to be executed.  The scheduler will keep a
     * reference to the task and return it later on to be processed.
     * @param task task to be scheduled
     */
    public void schedule(int i, Object obj);

    /**
     * Flags a task as done.  This way, we are able to remove the task from
     * the in-process list.
     * @param task task that was completed
     * @param contextId 
     */
    public void flagStop(WorkerTask task);

    /**
     * Returns a writer task to be processed
     * @return Task to be carried out
     * @throws TaskAssignmentException if all the work is done or no suitable
     * items are found for the moment.
     */
    public WorkerTask getWriterTask() throws TaskAssignmentException;

    /**
     * Returns a fetch task to be processed
     * @return Task to be carried out
     * @throws TaskAssignmentException if all the work is done or no suitable
     * items are found for the moment.
     */
    public WorkerTask getFetcherTask(int i) throws TaskAssignmentException;

    /**
     * Determines whether all the tasks are done.   If there are no more tasks
     * scheduled for process, and no ongoing tasks, it is impossible that new
     * work will arrive, so the spidering is done.
     * @return boolean value determining whether all work is done
     */
    public boolean allTasksDone();

    public void shutdown() ;
    
	TaskContext[] getTaskContexts();
	String status();
	public long getWriterJobsDone();
	public long getFetcherJobsDone();

}
