package com.tscp.mvne.hibernate;

import java.io.Serializable;

public class GeneralSPResponse implements Serializable {
  private static final long serialVersionUID = 1L;
  private int mvnemsgcode;
  private String status;
  private String mvnemsg;

  public GeneralSPResponse() {
    // do nothing
  }

  public int getMvnemsgcode() {
    return mvnemsgcode;
  }

  public void setMvnemsgcode(int mvnemsgcode) {
    this.mvnemsgcode = mvnemsgcode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMvnemsg() {
    return mvnemsg;
  }

  public void setMvnemsg(String mvnemsg) {
    this.mvnemsg = mvnemsg;
  }

  public boolean success() {
    return getStatus() != null && getStatus().equals("Y");
  }

  @Override
  public String toString() {
    return "GeneralSPResponse [mvnemsgcode=" + mvnemsgcode + ", status=" + status + ", mvnemsg=" + mvnemsg + "]";
  }

}