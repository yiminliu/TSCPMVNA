package com.tscp.mvne.billing.exception;

import com.tscp.mvne.exception.MVNEException;

public class BillingException extends MVNEException {
  private static final long serialVersionUID = 3743360978816790260L;
  protected int accountNo;
  protected String externalId;

  public BillingException() {
    super();
  }

  public BillingException(String methodName, String message, Throwable cause) {
    super(methodName, message, cause);
  }

  public BillingException(String methodName, String message) {
    super(methodName, message);
  }

  public BillingException(String message, Throwable cause) {
    super(message, cause);
  }

  public BillingException(String message) {
    super(message);
  }

  public BillingException(Throwable cause) {
    super(cause);
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

}