package com.tscp.mvne.network.exception;

import com.tscp.mvne.exception.MVNEException;
import com.tscp.mvne.network.NetworkInfo;

public class NetworkException extends MVNEException {
  private static final long serialVersionUID = -7412354082633059506L;
  protected NetworkInfo networkInfo;

  public NetworkException() {
    super();
  }

  public NetworkException(String methodName, String message, Throwable cause) {
    super(methodName, message, cause);
  }

  public NetworkException(String methodName, String message) {
    super(methodName, message);
  }

  public NetworkException(String message, Throwable cause) {
    super(message, cause);
  }

  public NetworkException(String message) {
    super(message);
  }

  public NetworkException(Throwable cause) {
    super(cause);
  }

  public NetworkInfo getNetworkinfo() {
    return networkInfo;
  }

  public void setNetworkinfo(NetworkInfo networkInfo) {
    this.networkInfo = networkInfo;
  }

}
