package net.javacoding.xsearch.core.component;

public class ProgressStatus extends TaskQueueEntry {

	public int id; //the id of the task queue

	public int type; //the type of the task
	public final static int MAIN_QUERY = 0;
	public final static int DELETION_LIST_QUERY = 1; //the task to get ids to be deleted
	public final static int FULL_LIST_QUERY = 2; //The task to get full list of all ids
	public final static int SUBSEQUENT_QUERY = 3;
	public final static int WRITE_DOCUMENT = 4;

	public int status;
	public final static int FINAL_TASK_FINISHED = 0;
	public final static int MAIN_QUERY_FINISHED = 1;
	//this is set by task in previous queue, tell current queue not to wait for more documents
	//used for batch processing subsequent queries
	public final static int ALL_DOCUMENTS_COMPLETED = 2;

	public ProgressStatus(int id, int type, int status) {
		super();
		this.id = id;
		this.type = type;
		this.status = status;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
