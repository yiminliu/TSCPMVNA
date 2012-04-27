package com.tscp.mvne.exception;

import javax.xml.ws.WebServiceException;

public class MVNEException extends WebServiceException {
  private static final long serialVersionUID = -1984305003999836500L;
  protected int transactionId;
  protected String methodName;

  public MVNEException() {
    super();
  }

  public MVNEException(String message, Throwable cause) {
    super(message, cause);
  }

  public MVNEException(String message) {
    super(message);
  }

  public MVNEException(Throwable cause) {
    super(cause);
  }

  // custom constructor
  public MVNEException(String methodName, String message) {
    super(message);
    this.methodName = methodName;
  }

  // custom constructor
  public MVNEException(String methodName, String message, Throwable cause) {
    super(message, cause);
    this.methodName = methodName;
  }

  public int getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(int transactionId) {
    this.transactionId = transactionId;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

}
