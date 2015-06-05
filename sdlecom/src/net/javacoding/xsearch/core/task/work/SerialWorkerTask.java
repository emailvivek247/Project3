package net.javacoding.xsearch.core.task.work;

import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.list.FastFetchFullDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FetchDeletedDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FetchFullDocumentListBySQLTask;

/**
 * Place holder task for tasks that needs to be executed sequentially.
 * The tail tasks will not execute until the head task and head task created tasks, like subsequent queries, are finished 
 * 
 * Current thread pool will take any task and run with it, which is not suitable for sequential tasks.
 *
 */
public class SerialWorkerTask extends BaseWorkerTaskImpl {

    List<WorkerTask> taskList = new ArrayList<WorkerTask>();

    public SerialWorkerTask(Scheduler scheduler) {
        super(WORKERTASK_RETRIEVERTASK, scheduler);
    }
    
    public void addWorkTask(WorkerTask t){
        taskList.add(t);
    }

    public int getType() {
        if (taskList.size() > 0) {
            return taskList.get(0).getType();
        }
        return WORKERTASK_RETRIEVERTASK;
    }

    public void prepare() {
    }

    public void execute() {
        if(taskList.size()>0){
            WorkerTask head = taskList.get(0);
            head.prepare();
            head.execute();
            head.stop();
            
            if(taskList.size()>1){
                StringBuilder sb = new StringBuilder();
                for(int i=1;i<taskList.size();i++){
                    if(i!=1){
                        sb.append(",");
                    }
                    sb.append(workerTask2String(taskList.get(i)));
                }
                this.scheduler.schedule(0, sb.toString());
            }
        }
    }
    
    public static SerialWorkerTask string2SerialWorkerTask(String input, IndexerContext ic){
        if(input==null) return null;
        String[] tasks = input.split(",");
        if(tasks.length>0){
            SerialWorkerTask swt = new SerialWorkerTask(ic.getScheduler());
            for(String t: tasks){
                if(FetchDeletedDocumentListBySQLTask.class.getName().equals(t)){
                    swt.addWorkTask(new FetchDeletedDocumentListBySQLTask(ic));
                }else if(FetchFullDocumentListBySQLTask.class.getName().equals(t)){
                    swt.addWorkTask(new FetchFullDocumentListBySQLTask(ic));
                }else if(FastFetchFullDocumentListBySQLTask.class.getName().equals(t)){
                    swt.addWorkTask(new FastFetchFullDocumentListBySQLTask(ic));
                }
            }
            return swt;
        }
        return null;
    }
    public static String workerTask2String(WorkerTask t){
        if(t==null) return null;
        return t.getClass().getName();
    }
}
