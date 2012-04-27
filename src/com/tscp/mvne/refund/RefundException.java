package com.tscp.mvne.refund;

import com.tscp.mvne.exception.MVNEException;

public class RefundException extends MVNEException {
  private static final long serialVersionUID = 4018439343134758689L;

  public RefundException() {
    super();
  }

  public RefundException(String methodName, String message, Throwable cause) {
    super(methodName, message, cause);
  }

  public RefundException(String methodName, String message) {
    super(methodName, message);
  }

  public RefundException(String message, Throwable cause) {
    super(message, cause);
  }

  public RefundException(String message) {
    super(message);
  }

  public RefundException(Throwable cause) {
    super(cause);
  }

}
