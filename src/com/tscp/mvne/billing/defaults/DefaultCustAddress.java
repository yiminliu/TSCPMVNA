package com.tscp.mvne.billing.defaults;

import com.telscape.billingserviceinterface.CustAddress;
import com.tscp.mvne.config.PROVISION;

public class DefaultCustAddress extends CustAddress {

  public DefaultCustAddress() {
    setAddress1("");
    setAddress2("");
    setAddress3("");
    setCity("");
    setState("");
    setZip("");
    setCountryCode(PROVISION.SERVICE.COUNTRY.shortValue());
    setFranchiseTaxCode(PROVISION.SERVICE.SERVICE_FRANCHISE_TAX.shortValue());
    setCounty("");
  }
}