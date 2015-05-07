package net.javacoding.xsearch.core.task;

/**
 * Interface that will be implemented upon each Task to be carried out
 * by a Thread.
 *
 */
public interface Task {

    /**
     * Has the task executed.  The thread calling this method will do it's
     * time in there :).
     */
    public void execute();

}
