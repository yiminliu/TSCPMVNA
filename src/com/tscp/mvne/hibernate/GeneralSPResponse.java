package com.tscp.mvne.hibernate;

import java.io.Serializable;

public class GeneralSPResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	private int code;
	private String status;
	private String msg;

	public GeneralSPResponse() {
		// do nothing
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isSuccess() {
		return getStatus() != null && getStatus().equals("Y");
	}

	@Override
	public String toString() {
		return "GeneralSPResponse [code=" + code + ", status=" + status + ", msg=" + msg + "]";
	}

}