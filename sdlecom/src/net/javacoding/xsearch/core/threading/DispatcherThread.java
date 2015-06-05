package net.javacoding.xsearch.core.threading;

import net.javacoding.xsearch.core.task.DispatcherTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class DispatcherThread extends Thread {
    static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.core.threading.DispatcherThread");

    protected DispatcherTask task;
    protected WorkerThreadPool pool;

    public DispatcherThread(ThreadGroup group, String name, WorkerThreadPool pool) {
        super(group, name);
        this.pool = pool;
    }

    public void assign(DispatcherTask task) {
        this.task = task;
        start();
    }

    public void run() {
        synchronized (task) {
            try{
                task.execute();
            }catch(Exception e){
                logger.warn("Exception in Run Method", e);
                e.printStackTrace();
            }
            task.notify();
        }
    }


}
