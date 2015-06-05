package net.javacoding.xsearch.core.task.dispatch;


import net.javacoding.xsearch.core.task.DispatcherTask;


public abstract class BaseDispatchTaskImpl implements DispatcherTask {

    protected boolean running;

    public BaseDispatchTaskImpl() {
        running = true;
    }

}
