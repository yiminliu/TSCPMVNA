package com.tscp.mvne.device;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tscp.mvne.customer.DeviceException;
import com.tscp.mvne.customer.dao.GeneralSPResponse;
import com.tscp.mvne.hibernate.HibernateUtil;

public class Device implements Serializable {
  private static final long serialVersionUID = 4048156355651030312L;
  private int id;
  private int custId;
  private int statusId;
  private int accountNo;
  private String label;
  private String value;
  private String status;
  private Date modDate;
  private Date effectiveDate;
  private Date expirationDate;

  private DeviceAssociation association;

  public Device() {
    this.statusId = DeviceStatus.UNKNOWN.getValue();
    this.status = DeviceStatus.UNKNOWN.getDescription();
  }

  public void delete() {
    if (getId() <= 0) {
      throw new DeviceException("Device ID cannot be empty");
    }
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();

    Query q = session.getNamedQuery("del_device_info");
    q.setParameter("in_cust_id", getCustId());
    q.setParameter("in_device_id", getId());

    List<GeneralSPResponse> generalSPResponseList = q.list();

    if (generalSPResponseList != null) {
      for (GeneralSPResponse generalSPResponse : generalSPResponseList) {
        if (generalSPResponse.getStatus().equals("Y")) {
          setId(generalSPResponse.getMvnemsgcode());
        } else {
          session.getTransaction().rollback();
          throw new DeviceException(generalSPResponse.getMvnemsg());
        }
      }
    } else {
      session.getTransaction().rollback();
      throw new DeviceException("Error Saving Device information...");
    }

    session.getTransaction().commit();
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

  public void save() throws DeviceException {
    if (getCustId() <= 0) {
      throw new DeviceException("Customer Id has not been set");
    }
    if (getLabel() == null || getLabel().isEmpty()) {
      throw new DeviceException("Device Label cannot be empty");
    }
    String methodName = "";
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();
    Query q;
    if (getId() == 0) {
      // Insert
      methodName = "ins_device_info";
      q = session.getNamedQuery(methodName);
      q.setParameter("in_account_no", getAccountNo());
    } else {
      // update
      methodName = "upd_device_info";
      q = session.getNamedQuery(methodName);
      q.setParameter("in_device_id", getId());
      q.setParameter("in_device_status_id", getStatusId());
      q.setParameter("in_eff_date", getEffectiveDate() == null ? "" : getEffectiveDate());
      q.setParameter("in_exp_date", getExpirationDate() == null ? "" : getExpirationDate());
    }
    q.setParameter("in_cust_id", getCustId());
    q.setParameter("in_device_label", getLabel());
    q.setParameter("in_device_value", getValue());
    List<GeneralSPResponse> generalSPResponseList = q.list();

    if (generalSPResponseList != null) {
      for (GeneralSPResponse generalSPResponse : generalSPResponseList) {
        if (generalSPResponse.getStatus().equals("Y")) {
          setId(generalSPResponse.getMvnemsgcode());
        } else {
          session.getTransaction().rollback();
          throw new DeviceException(generalSPResponse.getMvnemsg());
        }
      }
    } else {
      session.getTransaction().rollback();
      throw new DeviceException("Error Saving Device information...");
    }
    session.getTransaction().commit();

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

  @Override
  public String toString() {
    return "Device [deviceId=" + id + ", custId=" + custId + ", accountNo=" + accountNo + ", deviceLabel=" + label + ", deviceValue=" + value
        + ", deviceStatusId=" + statusId + ", deviceStatus=" + status + ", modDate=" + modDate + ", effectiveDate=" + effectiveDate + ", expirationDate="
        + expirationDate + ", deviceAssociation=" + association + "]";
  }

}