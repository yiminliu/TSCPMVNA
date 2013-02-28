package com.tscp.mvne.billing;

import java.io.Serializable;
import java.util.Date;

public class UnlinkedAccount implements Serializable {
	private static final long serialVersionUID = 4806854786267790179L;
	private int custId;
	private int accountNo;
	private String email;
	private int transId;
	private Date transDate;

	public int getCustId() {
		return custId;
	}

	public void setCustId(
			int custId) {
		this.custId = custId;
	}

	public int getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(
			int accountNo) {
		this.accountNo = accountNo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(
			String email) {
		this.email = email;
	}

	public int getTransId() {
		return transId;
	}

	public void setTransId(
			int transId) {
		this.transId = transId;
	}

	public Date getTransDate() {
		return transDate;
	}

	public void setTransDate(
			Date transDate) {
		this.transDate = transDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
