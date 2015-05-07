package com.fdt.common.dto;

import java.io.Serializable;
import java.util.Date;


/**
 * This class is an General Service response with Status and Message request to Firm Level User Add/Update services.
 *
 * @author APatel
 *
 */
public class TransactionRequestDTO implements Serializable {

	private static final long serialVersionUID = 3332321310320467437L;

	private String userName;

	private  String nodeName;

	private Date fromDate;

	private Date toDate;

	/**
	 * Pagination Variables
	 * The next two variables are for pagination.
	 * startFrom indicates which row number to start from in the search process
	 * numberOfRecords indicates how many rows to be returned (starting from startFrom)
	 *
	 * For example : If there are 50 records , user has page of 10 records each and user is on page 2.
	 * Now if user clicks next -->
	 * 			startFrom will be 20 and numberOfRows returned will be 10
	 */
    private Integer startingFrom = 0;

    // In case use doesn't supply this value , all the records will be returned.
    private Integer numberOfRecords = Integer.MAX_VALUE;


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Integer getStartingFrom() {
		return startingFrom;
	}

	public void setStartingFrom(Integer startingFrom) {
		this.startingFrom = startingFrom;
	}

	public Integer getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(Integer numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	@Override
	public String toString() {
		return "TransactionRequestDTO [userName=" + userName + ", nodeName="
				+ nodeName + ", fromDate=" + fromDate + ", toDate=" + toDate
				+ ", startingFrom=" + startingFrom + ", numberOfRecords="
				+ numberOfRecords + "]";
	}
}