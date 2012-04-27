package com.tscp.mvne.customer;

import com.tscp.mvne.exception.MVNEException;

public class DeviceException extends MVNEException {
  private static final long serialVersionUID = -4677308121146962038L;

  public DeviceException() {
    super();
  }

  public DeviceException(String methodName, String message, Throwable cause) {
    super(methodName, message, cause);
  }

  public DeviceException(String methodName, String message) {
    super(methodName, message);
  }

  public DeviceException(String message, Throwable cause) {
    super(message, cause);
  }

  public DeviceException(String message) {
    super(message);
  }

  public DeviceException(Throwable cause) {
    super(cause);
  }

}
