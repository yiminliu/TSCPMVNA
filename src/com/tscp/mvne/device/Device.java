package com.tscp.mvne.device;

import java.io.Serializable;
import java.util.Date;

public class Device implements Serializable {
  private static final long serialVersionUID = 4048156355651030312L;
  private int id;
  private int accountNo;
  private int custId;
  private String label;
  private String value;
  private String status;
  private int statusId;
  private Date modDate;
  private Date effectiveDate;
  private Date expirationDate;
  private DeviceAssociation association;

  public Device() {
    this.statusId = DeviceStatus.UNKNOWN.getValue();
    this.status = DeviceStatus.UNKNOWN.getDescription();
  }

  public int getAccountNo() {
    return accountNo;
  }

  public DeviceAssociation getAssociation() {
    return association;
  }

  public int getCustId() {
    return custId;
  }

  public Date getEffectiveDate() {
    return effectiveDate;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public int getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public Date getModDate() {
    return modDate;
  }

  public String getStatus() {
    return status;
  }

  public int getStatusId() {
    return statusId;
  }

  public String getValue() {
    return value;
  }

  public void setAccountNo(int accountNo) {
    this.accountNo = accountNo;
  }

  public void setAssociation(DeviceAssociation association) {
    this.association = association;
  }

  public void setCustId(int custId) {
    this.custId = custId;
  }

  public void setEffectiveDate(Date effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setModDate(Date modDate) {
    this.modDate = modDate;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setStatusId(int statusId) {
    this.statusId = statusId;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Device save() {
    return DeviceDao.save(this);
  }

  public void delete() {
    DeviceDao.delete(this);
  }

}