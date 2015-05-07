package net.javacoding.xsearch.core.task.work;


import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;


public abstract class BaseWorkerTaskImpl implements WorkerTask {

    protected int type;
    protected Scheduler scheduler;
    protected int contextId = 0;

    public BaseWorkerTaskImpl(int type, Scheduler scheduler) {
        this.type = type;
        this.scheduler = scheduler;
    }

    public void prepare() {
	}
    public void stop() {
    }

	public void exit() {
        scheduler.flagStop(this);
    }

    public int getType() {
        return type;
    }
    
    public int getContextId(){
    	return contextId;
    }
    
    public int getTotalDocumentsDone(){
    	return 1;
    }

}
