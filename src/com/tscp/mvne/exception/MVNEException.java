package com.tscp.mvne.exception;

import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;

import com.tscp.mvne.exception.fault.FaultBean;

@WebFault(name = "MVNEException", targetNamespace = "http://mvne.tscp.com/fault", faultBean = "com.tscp.mvne.network.exception.FaultBean")
public class MVNEException extends WebServiceException {
  private static final long serialVersionUID = -1984305003999836500L;
  private int transactionId;
  private String methodName;
  private FaultBean faultInfo;

  public MVNEException(String message, FaultBean faultInfo) {
    super(message);
    this.faultInfo = faultInfo;
  }

  public MVNEException(String message, FaultBean faultInfo, Throwable cause) {
    super(message, cause);
    this.faultInfo = faultInfo;
  }

  public FaultBean getFaultInfo() {
    return faultInfo;
  }

  public MVNEException(String message, Throwable cause) {
    super(message, cause);
  }

  public MVNEException() {
    super();
  }

  public MVNEException(String message) {
    super(message);
  }

  public MVNEException(String methodName, String message) {
    super(message);
    setMethodname(methodName);
  }

  public MVNEException(String message, Exception e) {
    super(message + ". " + e.getMessage(), e.getCause());
  }

  public MVNEException(String methodName, String message, Exception e) {
    super(message + ". " + e.getMessage(), e.getCause());
    setMethodname(methodName);
  }

  public String getMethodname() {
    return methodName;
  }

  public int getTransactionid() {
    return transactionId;
  }

  public void setMethodname(String methodname) {
    this.methodName = methodname;
  }

  public void setTransactionid(int transactionid) {
    this.transactionId = transactionid;
  }

}
