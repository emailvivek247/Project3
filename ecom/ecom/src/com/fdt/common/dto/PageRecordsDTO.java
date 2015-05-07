package com.fdt.common.dto;

import java.io.Serializable;
import java.util.Collection;

/**
 * This class represents the records for Pagination on server side.
 *
 * @author APatel
 *
 */
public class PageRecordsDTO implements Serializable {


	private static final long serialVersionUID = -3479810745025976116L;

	/**
	 * When client/user is navigating pages, recordCount holds the count for total number of records
	 *
	 *   RecordCount is not the number of records in the list (records)
	 *   It is the total number of matching records (not returned) as per SearchCriteria.
	 *
	 *   for example If user has page size of 10, user is on page #2 and total matching records(not returned records) are 35 Then,
	 *   	The instance of this class will return recordCount = 35, and records will hold 10 records (records 11 to 20)
	 */
	private int recordCount;

	private Collection<?> records;

	/**
	 * JSON needs this constructor to initialize
	 */
	public PageRecordsDTO(){
	}

    public PageRecordsDTO(Collection<?> records, int count) {
        this.records = records;
        this.recordCount = count;
    }

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int count) {
		this.recordCount = count;
	}

	public Collection<?> getRecords() {
		return this.records;
	}

	public void setRecords(Collection<?> records) {
			this.records = records;
	}
	
	@Override
    public String toString() {
        return "PageRecordsDTO ["
        		+ "records=" + records 
        		+ ", recordCount=" + recordCount
                + "]";
    }
}