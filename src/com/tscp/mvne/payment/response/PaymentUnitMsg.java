package com.tscp.mvne.payment.response;

public enum PaymentUnitMsg {
  INVALID_CARD("Unsuccessful Charge AuthCode::INVALID CARD"), INVALID_CARD_NUM("Error AuthCode::Invalid Card Num"), INVALID_EXP_DATE(
      "Error AuthCode::Invalid Expiration Date"),

  ERR_DECLINED("Unsuccessful Charge AuthCode::DECLINED"), ERR_CVV("Unsuccessful Charge AuthCode::DECLINED CVV2"), ERR_EXPIRED(
      "Unsuccessful Charge AuthCode::EXPIRED CARD");

  /*
   * INTERNAL_TIMEOUT("Unsuccessful Charge AuthCode::Timeout"),
   * INTERNAL_BATCH("Unsuccessful Charge AuthCode::Batch in Progress."),
   * INTERNAL_PICKUP_CARD( "Unsuccessful Charge AuthCode::PICK UP CARD"),
   * INTERNAL_RETRY("Unsuccessful Charge AuthCode::PLEASE RETRY5270"),
   * INTERNAL_SERVER_NOT_ALLOWED(
   * "Unsuccessful Charge AuthCode::SERV NOT ALLOWED"),
   * INTERNAL_SETTLE_BCH("Unsuccessful Charge AuthCode::Must Settle Bch"),
   * INTERNAL_NO_ANSWER( "Unsuccessful Charge AuthCode::No Answer"),
   * INTERNAL_DATA("Unsuccessful Charge AuthCode::INVLD DATA5359"),
   * 
   * ORACLE_JAVA(
   * "ORA-29532: Java call terminated by uncaught Java exception: java.io.IOException: Server returned HTT AuthCode::null"
   * ), ORACLE_NUMERIC(
   * "ORA-06502: PL/SQL: numeric or value error\nORA-06512: at \"IVRK11.SP_PROCESS_TRUCONNECT_PAYMENT\", line AuthCode::null"
   * ), ORACLE_UNAVAILABLE(
   * "ORA-02068: following severe error from TSCIVRDB\nORA-01034: ORACLE not available\nORA-27101: shared me AuthCode::null"
   * );
   */

  private String msg;

  private PaymentUnitMsg(String msg) {
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }

}
