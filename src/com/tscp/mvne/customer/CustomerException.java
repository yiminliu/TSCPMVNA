package com.tscp.mvne.customer;

import com.tscp.mvne.exception.MVNEException;

public class CustomerException extends MVNEException {
  private static final long serialVersionUID = -8032831785338560217L;
  protected int custId;

  public CustomerException() {
    super();
  }

  public CustomerException(String methodName, String message, Throwable cause) {
    super(methodName, message, cause);
  }

  public CustomerException(String methodName, String message) {
    super(methodName, message);
  }

  public CustomerException(String message, Throwable cause) {
    super(message, cause);
  }

  public CustomerException(String message) {
    super(message);
  }

  public CustomerException(Throwable cause) {
    super(cause);
  }

  public int getCustId() {
    return custId;
  }

  public void setCustId(int custId) {
    this.custId = custId;
  }

}
