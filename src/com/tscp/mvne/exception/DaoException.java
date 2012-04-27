package com.tscp.mvne.exception;

public class DaoException extends RuntimeException {
  private static final long serialVersionUID = 1464376120169337740L;

  public DaoException() {
    super();
  }

  public DaoException(String message, Throwable cause) {
    super(message, cause);
  }

  public DaoException(String message) {
    super(message);
  }

  public DaoException(Throwable cause) {
    super(cause);
  }

}