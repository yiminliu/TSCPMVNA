package com.tscp.mvne.util.audit;


//@WebService
public class AuditTester {
  private static AuditService auditService = new AuditService();

  // @WebMethod
  public Status getStatus(int custId, int accountNo) {
    Status status = auditService.getStatus(custId, accountNo);
    return status;
  }

}
