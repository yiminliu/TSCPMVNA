package com.tscp.mvne.exception.fault;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "MVNEException", namespace = "http://mvne.tscp.com/fault")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "com.tscp.mvne.exception.MVNEException")
public class FaultBean {
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
