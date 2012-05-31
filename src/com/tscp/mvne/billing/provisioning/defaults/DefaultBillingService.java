package com.tscp.mvne.billing.provisioning.defaults;

import com.telscape.billingserviceinterface.BillingService;
import com.tscp.mvne.billing.defaults.DefaultBillingName;
import com.tscp.mvne.billing.defaults.DefaultCustAddress;
import com.tscp.mvne.config.PROVISION;
import com.tscp.util.DateUtils;

public class DefaultBillingService extends BillingService {

  public DefaultBillingService() {
    setAccountDateActive(DateUtils.getXMLCalendar());
    setAccountNo("");
    setCurrencyCode(PROVISION.SERVICE.CURRENCY.shortValue());
    setEMFConfigId(PROVISION.SERVICE.EMF_CONFIG.shortValue());
    setExrateClass(PROVISION.SERVICE.EXRATE_CLASS.shortValue());
    setExternalAccountNoType(PROVISION.SERVICE.EXTERNAL_ACCOUNT_TYPE.shortValue());
    setExternalId("");
    setExternalIdType(PROVISION.SERVICE.EXTERNAL_ID_TYPE.shortValue());
    setRateClassDefault(PROVISION.SERVICE.RATECLASS.shortValue());
    setSalesChannelId(PROVISION.SERVICE.SALES_CHANNEL.shortValue());
    setServiceAddr(new DefaultCustAddress());
    setServiceName(new DefaultBillingName());
    setServiceStartDate(DateUtils.getXMLCalendar());
    setSysDate(DateUtils.getXMLCalendar());
  }

}