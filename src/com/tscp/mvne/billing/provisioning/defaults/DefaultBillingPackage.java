package com.tscp.mvne.billing.provisioning.defaults;

import com.telscape.billingserviceinterface.Package;
import com.tscp.mvne.config.PROVISION;
import com.tscp.util.DateUtils;

public class DefaultBillingPackage extends Package {

  public DefaultBillingPackage() {
    setPackageId(PROVISION.PACKAGE.ID);
    setExternalIdType(PROVISION.PACKAGE.EXTERNAL_ID_TYPE.shortValue());
    setActiveDate(DateUtils.getXMLCalendar());
    setAccountNo("");
  }

  public DefaultBillingPackage(int accountNo) {
    setPackageId(PROVISION.PACKAGE.ID);
    setExternalIdType(PROVISION.PACKAGE.EXTERNAL_ID_TYPE.shortValue());
    setActiveDate(DateUtils.getXMLCalendar());
    setAccountNo(Integer.toString(accountNo));
  }
}