package com.tscp.mvne.device;

import java.io.Serializable;
import java.util.Date;

public class DeviceAssociation implements Serializable {
  private static final long serialVersionUID = 1L;
  private int deviceId;
  private int accountNo;
  private int subscrNo;
  private int status;
  private String value;
  private String externalId;
  private Date activeDate;
  private Date inactiveDate;
  private Date modDate;

  public DeviceAssociation() {
    activeDate = new Date();
    inactiveDate = new Date();
    modDate = new Date();
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

  public int getAccountNo() {
    return accountNo;
  }

  public void setAccountNo(int accountNo) {
    this.accountNo = accountNo;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public int getSubscrNo() {
    return subscrNo;
  }

  public void setSubscrNo(int subscrNo) {
    this.subscrNo = subscrNo;
  }

  public Date getActiveDate() {
    return activeDate;
  }

  public void setActiveDate(Date activeDate) {
    this.activeDate = activeDate;
  }

  public Date getInactiveDate() {
    return inactiveDate;
  }

  public void setInactiveDate(Date inactiveDate) {
    this.inactiveDate = inactiveDate;
  }

  public Date getModDate() {
    return modDate;
  }

  public void setModDate(Date modDate) {
    this.modDate = modDate;
  }

  @Override
  public String toString() {
    return "DeviceAssociation [deviceId=" + deviceId + ", accountNo=" + accountNo + ", externalId=" + externalId + ", subscrNo=" + subscrNo + ", activeDate="
        + activeDate + ", inactiveDate=" + inactiveDate + ", modDate=" + modDate + "]";
  }

  public void save() {
    DeviceAssociationDao.save(this);
  }

}
