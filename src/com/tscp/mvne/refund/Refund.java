package com.tscp.mvne.refund;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


//@Entity
//@Table(name="Refund")
public class Refund {
	
	private int transId;
	private Date refundDate;
	private String refundBy;
	private int refundReasonCode;
	private String notes;
	
	//@Id
	//@Column(name="trans_id")
	public int getTransId() {
		return transId;
	}
	
	public void setTransId(int transId) {
		this.transId = transId;
	}
	
	//@Column(name="refund_date")
	public Date getRefundDate() {
		return refundDate;
	}
	public void setRefundDate(Date refundDate) {
		this.refundDate = refundDate;
	}
	
	//@Column(name="refund_by")
	public String getRefundBy() {
		return refundBy;
	}
	public void setRefundBy(String refundBy) {
		this.refundBy = refundBy;
	}
	
	//@Column(name="refund_reason_code")
	public int getRefundReasonCode() {
		return refundReasonCode;
	}
	public void setRefundReasonCode(int refundReasonCode) {
		this.refundReasonCode = refundReasonCode;
	}
	
	//@Column(name="notes")
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
