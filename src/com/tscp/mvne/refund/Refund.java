package com.tscp.mvne.refund;

import java.util.Date;

public class Refund {
	
	private int transId;
	private Date refundDate;
	private String refundBy;
	private int refundReasonCode;
	private String notes;
	private String creditCardNo;
	private String confirmationNo;
	
	public int getTransId() {
		return transId;
	}
	
	public void setTransId(int transId) {
		this.transId = transId;
	}
	
	public Date getRefundDate() {
		return refundDate;
	}
	public void setRefundDate(Date refundDate) {
		this.refundDate = refundDate;
	}
	
	public String getRefundBy() {
		return refundBy;
	}
	public void setRefundBy(String refundBy) {
		this.refundBy = refundBy;
	}
	
	public int getRefundReasonCode() {
		return refundReasonCode;
	}
	public void setRefundReasonCode(int refundReasonCode) {
		this.refundReasonCode = refundReasonCode;
	}

	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCreditCardNo() {
		return creditCardNo;
	}

	public void setCreditCardNo(String creditCardNo) {
		this.creditCardNo = creditCardNo;
	}

	public String getConfirmationNo() {
		return confirmationNo;
	}

	public void setConfirmationNo(String confirmationNo) {
		this.confirmationNo = confirmationNo;
	}
	
	
}
