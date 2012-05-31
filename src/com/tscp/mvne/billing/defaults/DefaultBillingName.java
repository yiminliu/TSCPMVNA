package com.tscp.mvne.billing.defaults;

import com.telscape.billingserviceinterface.BillName;

public class DefaultBillingName extends BillName {

  public DefaultBillingName() {
    setFirstName("");
    setMiddleName("");
    setLastName("");
  }
}