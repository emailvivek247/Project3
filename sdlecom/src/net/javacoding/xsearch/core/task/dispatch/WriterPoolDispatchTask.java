package net.javacoding.xsearch.core.task.dispatch;

import net.javacoding.xsearch.core.exception.FetcheringDoneException;
import net.javacoding.xsearch.core.exception.TaskAssignmentException;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.threading.WorkerThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WriterPoolDispatchTask extends BaseDispatchTaskImpl {

    protected WorkerThreadPool writerPool;
    protected Scheduler scheduler;

    public WriterPoolDispatchTask(WorkerThreadPool writerPool, Scheduler scheduler) {
        super();
        this.writerPool = writerPool;
        this.scheduler = scheduler;
    }

    public void execute() {
        Logger logger = LoggerFactory.getLogger(this.getClass().getName());
        logger.debug("Writer task dispatcher running ...");
        while (running&&!this.scheduler.getIndexerContext().isStopping()) {
            try {
                WorkerTask wt = scheduler.getWriterTask();
                if(wt!=null)
                  writerPool.assign(wt);
            } catch (FetcheringDoneException e) {
                running = false;
                this.scheduler.getIndexerContext().setStopping();
            } catch (TaskAssignmentException e) {
                // We dealt with all subclasses, so this shouldn't happen !!!
            }
        }
        this.scheduler.getIndexerContext().setStopping();
        synchronized (writerPool) {
            writerPool.notifyAll();
        }
        logger.debug("Writer task dispatcher dying ...");
    }
}
