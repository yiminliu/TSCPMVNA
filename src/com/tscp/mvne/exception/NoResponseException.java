package com.tscp.mvne.exception;

public class NoResponseException extends RuntimeException {
  private static final long serialVersionUID = 8452903212547415398L;

  public NoResponseException() {
    super();
  }

  public NoResponseException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoResponseException(String message) {
    super(message);
  }

  public NoResponseException(Throwable cause) {
    super(cause);
  }

}
