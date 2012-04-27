package com.tscp.mvne.billing.contract.exception;

import com.tscp.mvne.exception.MVNEException;

public class ContractException extends MVNEException {
  private static final long serialVersionUID = 1464376120169337740L;

  public ContractException() {
    super();
  }

  public ContractException(String methodName, String message, Throwable cause) {
    super(methodName, message, cause);
  }

  public ContractException(String methodName, String message) {
    super(methodName, message);
  }

  public ContractException(String message, Throwable cause) {
    super(message, cause);
  }

  public ContractException(String message) {
    super(message);
  }

  public ContractException(Throwable cause) {
    super(cause);
  }

}
