package com.tscp.mvne.billing.provisioning.defaults;

import com.telscape.billingserviceinterface.PkgComponent;
import com.tscp.mvne.config.PROVISION;
import com.tscp.util.DateUtils;

public class DefaultBillingComponent extends PkgComponent {

  public DefaultBillingComponent() {
    setComponentId(PROVISION.COMPONENT.INSTALL);
    setPackageId(PROVISION.COMPONENT.PACKAGE_ID);
    setPackageInstanceId(PROVISION.PACKAGE.INSTANCE_ID.shortValue());
    setPackageInstanceIdServ(PROVISION.PACKAGE.INSTANCE_SERV_ID.shortValue());
    setComponentActiveDate(DateUtils.getXMLCalendar());
    setExternalId("");
    setExternalIdType(PROVISION.COMPONENT.EXTERNAL_ID_TYPE.shortValue());
    setComponentInstanceIdServ(PROVISION.COMPONENT.INSTANCE_SERV_ID.shortValue());
  }
}