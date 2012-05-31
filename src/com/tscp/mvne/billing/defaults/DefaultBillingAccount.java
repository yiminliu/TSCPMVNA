package com.tscp.mvne.billing.defaults;

import com.telscape.billingserviceinterface.BillName;
import com.telscape.billingserviceinterface.BillingAccount;
import com.telscape.billingserviceinterface.ContactInfo;
import com.telscape.billingserviceinterface.CustAddress;
import com.tscp.mvne.config.BILLING;
import com.tscp.util.DateUtils;

public class DefaultBillingAccount extends BillingAccount {

  public DefaultBillingAccount() {
    setAccountCategory(BILLING.accountCategory.shortValue());
    setBillDispMethod(BILLING.billDisplayMethod.shortValue());
    setBillFormatOpt(BILLING.billFormatOption);

    BillName billname = new BillName();
    billname.setFirstName("Shell Account");
    billname.setMiddleName("");
    billname.setLastName("TruConnect");
    setBillName(billname);

    setBillPeriod(BILLING.billPeriod.toString());
    setCCardIdServ(BILLING.defaultCreditCardIdServ.shortValue());
    setCollectionIndicator(BILLING.collectionIndicator.shortValue());

    ContactInfo contactinfo = new ContactInfo();
    contactinfo.setContact1Name("");
    contactinfo.setContact1Phone("");
    setContactInfo(contactinfo);

    setCreditThresh(BILLING.creditThreshold.toString());
    setCredStatus(BILLING.creditStatus.shortValue());
    setCurrencyCode(BILLING.currencyCode.shortValue());

    CustAddress custAddress = new CustAddress();
    custAddress.setAddress1("355 S Grand Ave");
    custAddress.setAddress2("");
    custAddress.setAddress3("");
    custAddress.setCity("Los Angeles");
    custAddress.setCountryCode(BILLING.customerCountryCode.shortValue());
    custAddress.setCounty("Los Angeles");
    custAddress.setFranchiseTaxCode(BILLING.customerFranchiseTaxCode.shortValue());
    custAddress.setState("CA");
    custAddress.setZip("90071");
    setCustAddress(custAddress);

    setBillAddress(custAddress);

    setCustEmail("tscwebgeek@telscape.net");
    setCustFaxNo("");
    setCustPhone1("2133880022");
    setCustPhone2("");

    setExrateClass(BILLING.exrateClass.shortValue());
    setExternalAccountNoType(BILLING.accountType.shortValue());
    setInsertGrpId(BILLING.insertGroupId.shortValue());

    setLanguageCode(BILLING.languageCode.shortValue());
    setMarketCode(BILLING.marketCode.shortValue());
    setMsgGroupId(BILLING.messageGroupId.shortValue());

    setOwningCostCtr(BILLING.owningCostCenter.shortValue());
    setPaymentMethod(BILLING.paymentMethod.shortValue());

    setRateClassDefault(BILLING.rateClassDefault.shortValue());

    // TODO Double check this field
    setServiceCenterId(BILLING.serviceCenterId.shortValue());
    setServiceCenterType(BILLING.serviceCenterType.shortValue());

    setSicCode(BILLING.sicCode.shortValue());
    setTieCode(BILLING.tieCode.shortValue());

    setVipCode(BILLING.vipCode.shortValue());

    setSysDate(DateUtils.getXMLCalendar());
    setAccountDateActive(DateUtils.getXMLCalendar());
  }
}