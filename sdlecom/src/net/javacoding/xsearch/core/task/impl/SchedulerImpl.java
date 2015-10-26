package net.javacoding.xsearch.core.task.impl;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.ArrayList;

import net.javacoding.queue.DiskBackedQueue;
import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.exception.FetcheringDoneException;
import net.javacoding.xsearch.core.exception.TaskAssignmentException;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.ESWriteDocumentToIndexTask;
import net.javacoding.xsearch.core.task.work.SerialWorkerTask;
import net.javacoding.xsearch.core.task.work.WriteDocumentToIndexTask;
import net.javacoding.xsearch.core.task.work.subsequent.FetchDocumentBySQLTask;
import net.javacoding.xsearch.core.task.work.subsequent.FetchDocumentsInBatchTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.admin.ui.action.constants.IndexType;

public class SchedulerImpl implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerImpl.class);

    protected IndexerContext ic;

    public class TaskContext {
        /*
         * All tasks or objects are put into queues Each queue is for storing
         * results of previous Main Query, Subsequent Query Each queue also
         * serves as inputs for the current task
         */
        DiskBackedQueue taskQueue;
        int threadCount;
        long taskDone;
        long documentDone;
        long documentReceived;
        int maxThreadCount = Integer.MAX_VALUE;
    }

    protected TaskContext[] taskContexts;

    public IndexerContext getIndexerContext() {
        return ic;
    }

    public SchedulerImpl(IndexerContext ic) {
        this.ic = ic;
        try {
            Dataquery[] contentQueries = ic.getDatasetConfiguration().getContentDataqueryArray();
            int taskQueueCount = contentQueries.length + 2;
            this.taskContexts = new TaskContext[taskQueueCount];
            for (int i = 0; i < taskQueueCount; i++) {
                taskContexts[i] = new TaskContext();
                if (i == 0) {
                    taskContexts[i].taskQueue = new DiskBackedQueue(
                            ic.getDatasetConfiguration().getWorkDirectoryFile(), "result" + i, false, 5);
                    taskContexts[i].maxThreadCount = 1;
                } else if (i == taskQueueCount - 1) {
                    taskContexts[i].taskQueue = new DiskBackedQueue(
                            ic.getDatasetConfiguration().getWorkDirectoryFile(), "result" + i, false, 10);
                    taskContexts[i].maxThreadCount = ic.getDatasetConfiguration().getWriterThreadsCount();
                } else {
                    // if i is 1 ~ (taskQueueCount-2), the content query for it
                    // is (i-1)
                    int bufferSize = ic.getDatasetConfiguration().getFetcherThreadsCount()
                            * contentQueries[i - 1].getBatchSize();
                    if (bufferSize < 10) {
                        bufferSize = 100;
                    }
                    taskContexts[i].taskQueue = new DiskBackedQueue(
                            ic.getDatasetConfiguration().getWorkDirectoryFile(), "result" + i, false, bufferSize);
                    if (contentQueries[i - 1].getIsBatchNeeded()) {
                        taskContexts[i].maxThreadCount = ic.getDatasetConfiguration().getBatchFetcherThreadsCount();
                    } else {
                        taskContexts[i].maxThreadCount = ic.getDatasetConfiguration().getFetcherThreadsCount();
                    }
                }
            }
            // writerTaskQueue = taskQueues[taskQueueCount-1];
        } catch (java.io.IOException ioe) {
            logger.error(NOTIFY_ADMIN, "can not create queue files under directory "
                    + ic.getDatasetConfiguration().getWorkDirectoryFile(), ioe);
        }
    }

    private void stopPipeline() throws FetcheringDoneException {
        for (int i = 0; i < taskContexts.length; i++) {
            synchronized (taskContexts[i]) {
                taskContexts[i].notifyAll();
            }
        }
        throw new FetcheringDoneException();
    }

    public void shutdown() {
        for (int i = 0; i < taskContexts.length; i++) {
            taskContexts[i].taskQueue.disconnect();
        }
    }

    private void assertRange(int i) {
        if (i >= taskContexts.length || i < 0) {
            throw new RuntimeException("schedule into an out-of-range queue!");
        }
    }

    /**
     * Schedules an object to a queue to be processed.
     * 
     * @param int queue number to be scheduled, 0 is the first
     * @param obj
     *            object to be scheduled
     */
    public void schedule(int i, Object obj) {
        assertRange(i);
        synchronized (taskContexts[i]) {
            taskContexts[i].taskQueue.enqueue(obj);
            taskContexts[i].documentReceived++;
            taskContexts[i].notify();
        }
    }

    public void flagStop(WorkerTask task) {
        // allTaskDone() will check assigned tasks
        // if all done, getxxxTask() will notify events to finish
        // but sometimes the task will flagDone() after getxxxTask(), causing
        // deadlock waiting on the events
        // so need to notify the events first
        synchronized (taskContexts[task.getContextId()]) {
            taskContexts[task.getContextId()].taskDone++;
            taskContexts[task.getContextId()].threadCount--;
            taskContexts[task.getContextId()].documentDone += task.getTotalDocumentsDone();
            taskContexts[task.getContextId()].notify();
        }
    }

    public WorkerTask getWriterTask() throws TaskAssignmentException {
        int i = taskContexts.length - 1;
        synchronized (taskContexts[i]) {
            while (taskContexts[i].taskQueue.length() <= 0 || taskContexts[i].threadCount > taskContexts[i].maxThreadCount) {
                try {
                    if (this.getIndexerContext().isStopping())
                        return null;
                    if (allTasksDone()) {
                        stopPipeline();
                    }
                    taskContexts[i].wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WorkerTask task = createWriterTask(taskContexts[i].taskQueue.dequeue());
            if (task != null) {
                taskContexts[i].threadCount++;
                return task;
            }
        }
        return null;
    }

    /**
     * Returns a fetch task of a subsequent query to be carried out.
     * 
     * @return WorkerTask task to be done
     * @throws TaskAssignmentException
     *             notifies when the work is done or there are no current
     *             outstanding tasks.
     */
    public WorkerTask getFetcherTask(int i) throws TaskAssignmentException {
        assert i < taskContexts.length - 1; // this should not be a writer task
        synchronized (taskContexts[i]) {
            while (taskContexts[i].taskQueue.length() <= 0
                    || taskContexts[i].threadCount > taskContexts[i].maxThreadCount) {
                try {
                    if (this.getIndexerContext().isStopping())
                        return null;
                    taskContexts[i].wait(999);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WorkerTask task = createFetcherTask(taskContexts[i].taskQueue, i);
            if (task != null) {
                taskContexts[i].threadCount++;
                return task;
            }
        }
        return null;
    }

    /**
     * Determines whether all the tasks are done. If there are no more tasks
     * scheduled for process, and no ongoing tasks, it is impossible that new
     * work will arrive, so the spidering is done.
     * 
     * @return boolean value determining whether all work is done
     */
    public boolean allTasksDone() {
        for (int i = 0; i < taskContexts.length; i++) {
            synchronized (taskContexts[i]) {
                if (taskContexts[i].taskQueue.length() + taskContexts[i].threadCount > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private WorkerTask createWriterTask(Object obj) {
        WorkerTask task = null;
        if (obj instanceof TextDocument) {
            IndexType indexType = ic.getDatasetConfiguration().getIndexType();
            if (indexType == null || indexType == IndexType.LUCENE) {
                task = new WriteDocumentToIndexTask(this, (TextDocument) obj, taskContexts.length - 1);
            } else if (indexType == IndexType.ELASTICSEARCH) {
                task = new ESWriteDocumentToIndexTask(this, (TextDocument) obj, taskContexts.length - 1);
            }
        } else if (obj instanceof WorkerTask) {
            task = (WorkerTask) obj;
        } else {
            logger.warn("Failed to create fetch task, object type from queue is:" + obj.getClass().getName());
        }
        return task;
    }

    private WorkerTask createFetcherTask(DiskBackedQueue queue, int i) {
        if (i == 0) {
            return createListFetcherTask(queue.dequeue());
        } else {
            return createDetailFetcherTask(queue, i);
        }
    }

    private WorkerTask createListFetcherTask(Object obj) {
        WorkerTask task = null;
        if (obj instanceof WorkerTask) {
            task = (WorkerTask) obj;
        } else if (obj instanceof String) {
            task = SerialWorkerTask.string2SerialWorkerTask((String) obj, ic);
        } else {
            logger.warn("Failed to create fetch task, object type from queue is:" + obj.getClass().getName());
        }
        return task;
    }

    private WorkerTask createDetailFetcherTask(DiskBackedQueue queue, int i) {
        // always i>0
        Dataquery dq = this.ic.getDatasetConfiguration().getContentDataqueries().get(i - 1);
        if (!dq.getIsBatchNeeded()) {
            return createSingleDetailFetcherTask(queue, i);
        } else {
            return createBatchDetailFetcherTask(queue, i, dq.getBatchSize());
        }
    }

    private WorkerTask createSingleDetailFetcherTask(DiskBackedQueue queue, int i) {
        Object obj = queue.dequeue();
        WorkerTask task = null;
        if (obj instanceof TextDocument) {
            task = new FetchDocumentBySQLTask(this, i, (TextDocument) obj);
        } else if (obj instanceof WorkerTask) {
            task = (WorkerTask) obj;
        } else {
            logger.warn("Failed to create fetch task, object type from queue is:" + obj.getClass().getName());
        }
        return task;
    }

    private WorkerTask createBatchDetailFetcherTask(DiskBackedQueue queue, int i, int batchSize) {
        synchronized (queue) {
            // do nothing if not all documents are received or batch size is not
            // reached
            if (queue.length() < batchSize && !previousQueueEmpty(i))
                return null;
            ArrayList<TextDocument> docs = new ArrayList<TextDocument>(batchSize);
            for (int x = 0; x < batchSize && !queue.isEmpty(); x++) {
                Object o = queue.dequeue();
                if (o instanceof TextDocument) {
                    docs.add((TextDocument) o);
                } else {
                    logger.warn("should never come to here!");
                }
            }
            if (docs.size() == 0)
                return null;
            return new FetchDocumentsInBatchTask(this, i, docs.toArray(new TextDocument[docs.size()]));
        }
    }

    private boolean previousQueueEmpty(int i) {
        return i == 0 ? true : taskContexts[i - 1].taskQueue.length() <= 0;
    }

    public TaskContext[] getTaskContexts() {
        return taskContexts;
    }

    public String status() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < taskContexts.length; i++) {
            if (i == 0) {
                sb.append(taskContexts[i + 1].documentReceived);
                sb.append("|");
            } else if (i == taskContexts.length - 1) {
                sb.append(taskContexts[i].taskQueue.length());
                sb.append(":");
                sb.append(taskContexts[i].threadCount);
                sb.append("|");
                sb.append(taskContexts[i].documentDone);
            } else {
                sb.append(taskContexts[i].taskQueue.length());
                sb.append(":");
                sb.append(taskContexts[i].threadCount);
                sb.append("|");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public long getFetcherJobsDone() {
        int i = taskContexts.length - 1;
        return taskContexts[i].documentReceived;
    }

    public long getWriterJobsDone() {
        int i = taskContexts.length - 1;
        return taskContexts[i].taskDone;
    }
}
