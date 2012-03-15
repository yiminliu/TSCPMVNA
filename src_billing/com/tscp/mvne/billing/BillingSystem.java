package com.tscp.mvne.billing;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceException;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tscp.mvne.billing.api.ArrayOfMessageHolder;
import com.tscp.mvne.billing.api.ArrayOfPackage;
import com.tscp.mvne.billing.api.ArrayOfPackageHolder;
import com.tscp.mvne.billing.api.ArrayOfPaymentHolder;
import com.tscp.mvne.billing.api.ArrayOfPkgComponent;
import com.tscp.mvne.billing.api.ArrayOfServiceHolder;
import com.tscp.mvne.billing.api.ArrayOfValueHolder;
import com.tscp.mvne.billing.api.BillName;
import com.tscp.mvne.billing.api.BillNameHolder;
import com.tscp.mvne.billing.api.BillingAccount;
import com.tscp.mvne.billing.api.BillingAddressHolder;
import com.tscp.mvne.billing.api.BillingService;
import com.tscp.mvne.billing.api.BillingServiceInterfaceSoap;
import com.tscp.mvne.billing.api.ContactInfo;
import com.tscp.mvne.billing.api.ContactInfoHolder;
import com.tscp.mvne.billing.api.CustAddress;
import com.tscp.mvne.billing.api.CustBalanceHolder;
import com.tscp.mvne.billing.api.MessageHolder;
import com.tscp.mvne.billing.api.PackageHolder;
import com.tscp.mvne.billing.api.PaymentHolder;
import com.tscp.mvne.billing.api.PkgComponent;
import com.tscp.mvne.billing.api.ServiceHolder;
import com.tscp.mvne.billing.api.UsageHolder;
import com.tscp.mvne.billing.api.ValueHolder;
import com.tscp.mvne.billing.exception.BillingException;
import com.tscp.mvne.billing.exception.ProvisionException;
import com.tscp.mvne.billing.provisioning.ProvisionUtil;
import com.tscp.mvne.billing.service.BillingServiceProvider;
import com.tscp.mvne.config.Config;
import com.tscp.mvne.hibernate.HibernateUtil;

public class BillingSystem {
  // 1 = Port 5 = CR-D 220 = Pre-Paid
  protected static short discReason = 5;
  protected static final BillingServiceInterfaceSoap port = BillingServiceProvider.getInstance();
  private static final String USERNAME = "TSCPMVNE.BillingSystem";
  private static Properties props = new Properties();

  public BillingSystem() {
    loadDefaults();
  }

  private static void loadDefaults() {
    try {
      props.clear();
      props.load(BillingSystem.class.getClassLoader().getResourceAsStream(Config.provisionFile));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Deprecated
  public void addComponent(Account account, ServiceInstance serviceinstance, Package iPackage, Component componentid)
      throws BillingException {
    if (account == null || account.getAccountno() <= 0) {
      throw new BillingException("addComponent", "Error adding service to unknown Account");
    }
    if (serviceinstance == null || serviceinstance.getExternalId() == null
        || serviceinstance.getExternalId().trim().length() <= 0) {
      throw new BillingException("addComponent", "Please include a service to be added...");
    }
    if (iPackage == null || iPackage.getInstanceId() == 0) {
      throw new BillingException("addComponent", "Component must be added to a valid Package");
    }
    PkgComponent pkgComponent = ProvisionUtil.getDefaultBillingComponent();
    if (componentid == null) {
      componentid = new Component();
      componentid.setId(0);
    }
    if (componentid != null && componentid.getId() > 0) {
      pkgComponent.setComponentId(componentid.getId());
    } else {
      componentid.setId(pkgComponent.getComponentId());
    }
    pkgComponent.setExternalId(serviceinstance.getExternalId());
    if (iPackage != null && iPackage.getId() > 0) {
      pkgComponent.setPackageId(iPackage.getId());
    }
    pkgComponent.setPackageInstanceId(iPackage.getInstanceId());
    pkgComponent.setPackageInstanceIdServ(Short.parseShort(iPackage.getInstanceIdServ()));

    ArrayOfPkgComponent componentList = new ArrayOfPkgComponent();
    componentList.getPkgComponent().add(pkgComponent);

    ArrayOfMessageHolder messageHolder = port.addComponent(USERNAME, componentList);
    if (messageHolder == null) {
      throw new BillingException("addPackage", "No response returned from billing system");
    }
    if (messageHolder.getMessageHolder() != null && messageHolder.getMessageHolder().size() > 0) {
      for (MessageHolder message : messageHolder.getMessageHolder()) {
        if (message.getStatus().equals("Success")) {
        } else {
          throw new BillingException("addPackage", "Error adding component " + pkgComponent.getComponentId() + " to Account "
              + account.getAccountno() + ". Returned message is " + message.getMessage());
        }
      }
    }
  }

  @Deprecated
  public void addPackage(Account account, ServiceInstance serviceinstance, Package iPackage) throws BillingException {
    if (account == null || account.getAccountno() <= 0) {
      throw new BillingException("addServiceInstance", "Error adding service to unknown Account");
    }
    if (serviceinstance == null || serviceinstance.getExternalId() == null
        || serviceinstance.getExternalId().trim().length() <= 0) {
      throw new BillingException("addServiceInstance", "Please include a service to be added...");
    }
    com.tscp.mvne.billing.api.Package kenanPackage = ProvisionUtil.getDefaultBillingPackage();

    kenanPackage.setAccountNo(Integer.toString(account.getAccountno()));

    if (iPackage == null) {
      iPackage = new Package();
      iPackage.setId(0);
    }
    if (iPackage != null && iPackage.getId() > 0) {
      kenanPackage.setPackageId(iPackage.getId());
    } else {
      iPackage.setId(kenanPackage.getPackageId());
    }

    ArrayOfPackage packages = new ArrayOfPackage();
    packages.getPackage().add(kenanPackage);

    ArrayOfValueHolder valueHolder = port.addPackage(USERNAME, packages);
    if (valueHolder == null) {
      throw new BillingException("addPackage", "No response returned from billing system");
    }
    if (valueHolder.getValueHolder() != null && valueHolder.getValueHolder().size() > 0) {
      for (ValueHolder value : valueHolder.getValueHolder()) {
        if (value.getStatusMessage().getStatus().equals("Success")) {
          System.out.println("Added Package " + iPackage.getId() + " :: Kenan returned ID -> " + value.getValue().trim()
              + " with IDServ -> " + value.getValue2());
          iPackage.setInstanceId(Integer.parseInt(value.getValue().trim()));
          iPackage.setInstanceIdServ(value.getValue2().trim());
        } else {
          throw new BillingException("addPackage", "Error adding package " + kenanPackage.getPackageId() + " to Account "
              + account.getAccountno() + ". Returned message is " + value.getStatusMessage().getMessage());
        }
      }
    }
  }

  public void addPayment(Account account, String paymentAmount) throws BillingException {

    String externalId = Integer.toString(account.getAccountno());
    int externalIdType = 1;
    String amount = paymentAmount;
    XMLGregorianCalendar transDate = sysdate();
    int transType = Integer.parseInt(props.getProperty("payment.trans_type"));
    String submitBy = "tcweb";

    MessageHolder message = port.addPayment("api", externalId, externalIdType, amount, transDate, transType, submitBy);
    if (message != null) {
      System.out.println("Status  :: " + message.getStatus());
      System.out.println("Message :: " + message.getMessage());
      if (!message.getStatus().equals("Success")) {
        throw new BillingException("addPayment", "Error adding Payment $" + Double.parseDouble(paymentAmount) / 100
            + " to Account " + account.getAccountno() + ". Return Message is :: " + message.getMessage());
      }
    } else {
      throw new BillingException("addPayment", "No Response from the Billing Unit...");
    }

  }

  @Deprecated
  public void addServiceInstance(Account account, ServiceInstance serviceinstance) throws BillingException {
    if (account == null || account.getAccountno() <= 0) {
      throw new BillingException("addServiceInstance", "Error adding service to unknown Account");
    }
    if (serviceinstance == null || serviceinstance.getExternalId() == null
        || serviceinstance.getExternalId().trim().length() <= 0) {
      throw new BillingException("addServiceInstance", "Please include a service to be added...");
    }
    if (account.getFirstname() == null || account.getFirstname().trim().length() == 0) {
      bindAccountObject(account);
    }
    boolean contains = false;
    // if( account.getServiceinstancelist().contains(arg0))
    for (ServiceInstance si : account.getServiceinstancelist()) {
      if (si.getExternalId().equals(serviceinstance.getExternalId().trim())) {
        contains = true;
        break;
      }
    }
    if (!contains) {
      BillingService billingService = ProvisionUtil.getDefaultBillingService();
      billingService.setAccountNo(Integer.toString(account.getAccountno()));

      billingService.getServiceName().setFirstName(account.getFirstname());
      billingService.getServiceName().setMiddleName(account.getMiddlename());
      billingService.getServiceName().setLastName(account.getLastname());

      billingService.getServiceAddr().setAddress1(account.getContact_address1());
      billingService.getServiceAddr().setAddress2(account.getContact_address2());
      billingService.getServiceAddr().setCity(account.getContact_city());
      billingService.getServiceAddr().setState(account.getContact_state());
      billingService.getServiceAddr().setZip(account.getContact_zip());

      billingService.setExternalId(serviceinstance.getExternalId().trim());

      MessageHolder message = port.addService(USERNAME, billingService);
      if (message == null) {
        throw new BillingException("addServiceInstance", "No response returned from foreign billing system.");
      } else {
        if (!message.getStatus().equals("Success")) {
          throw new BillingException("addServiceInstance", "Error adding ServiceInstance " + serviceinstance.getExternalId()
              + " to account " + account.getAccountno() + "..." + message.getMessage());
        }
      }
    } else {
      System.out.println("Service is already associated with this account...Skipping add...");
      throw new ProvisionException("Service is already associated with this account...Skipping add...");
    }
  }

  private void bindAccountObject(Account account) {
    BillName billName = getBillName(account.getAccountno());
    account.setFirstname(billName.getFirstName());
    account.setMiddlename(billName.getMiddleName());
    account.setLastname(billName.getLastName());

    CustAddress custAddress = getCustAddress(account.getAccountno());
    account.setContact_address1(custAddress.getAddress1());
    account.setContact_address2(custAddress.getAddress2());
    account.setContact_city(custAddress.getCity());
    account.setContact_state(custAddress.getState());
    account.setContact_zip(custAddress.getZip());
  }

  public int createAccount(Account account) throws BillingException {
    // default the values in Kenan to the properties file declaration
    BillingAccount billingAccount = getBillingAccountDefault();

    billingAccount.getBillName().setFirstName(account.getFirstname());
    billingAccount.getBillName().setMiddleName(account.getMiddlename());
    billingAccount.getBillName().setLastName(account.getLastname());

    billingAccount.getCustAddress().setAddress1(account.getContact_address1());
    billingAccount.getCustAddress().setAddress2(account.getContact_address2());
    billingAccount.getCustAddress().setCity(account.getContact_city());
    billingAccount.getCustAddress().setState(account.getContact_state());
    billingAccount.getCustAddress().setZip(account.getContact_zip());

    billingAccount.getBillAddress().setAddress1(account.getContact_address1());
    billingAccount.getBillAddress().setAddress2(account.getContact_address2());
    billingAccount.getBillAddress().setCity(account.getContact_city());
    billingAccount.getBillAddress().setState(account.getContact_state());
    billingAccount.getBillAddress().setZip(account.getContact_zip());

    billingAccount.setCustEmail(account.getContact_email());

    billingAccount.setCustPhone1(account.getContact_number());

    ValueHolder response = port.addAccount(USERNAME, billingAccount);
    if (response == null) {
      throw new BillingException("createAccount", "No response returned from Billing System...");
    } else {
      if (response.getValue() == null || response.getValue().trim().length() == 0) {
        if (response.getStatusMessage() != null) {
          throw new BillingException("createAccount", "Account Number has not been returned.."
              + response.getStatusMessage().getStatus() + " " + response.getStatusMessage().getMessage());
        }
        throw new BillingException("createAccount", "Account Number has not been returned..");
      } else {
        account.setAccountno(Integer.parseInt(response.getValue().trim()));
      }
    }

    return account.getAccountno();
  }

  public void deleteServiceInstance(Account account, ServiceInstance serviceinstance) throws BillingException {
    System.out.println("Disconnecting Service on Account " + account.getAccountno() + " and ServiceInstance "
        + serviceinstance.getExternalId());
    if (account == null || account.getAccountno() == 0) {
      throw new BillingException("Please specify an account to delete this service against");
    }
    if (serviceinstance == null || serviceinstance.getExternalId() == null
        || serviceinstance.getExternalId().trim().length() == 0) {
      throw new BillingException("deleteServiceInstance", "Please specify a service to be disconnected...");
    }
    MessageHolder message = port.disconnectServicePackages(USERNAME, Integer.toString(account.getAccountno()), serviceinstance
        .getExternalId(), serviceinstance.getExternalIdType(), sysdate(), discReason);
    // MessageHolder message = port.disconnectService(USERNAME,
    // serviceinstance.getExternalid(), serviceinstance.getExternalidtype(),
    // sysdate(), discReason);
    if (message == null) {
      throw new BillingException("deleteServiceInstance", "No response returned from foreign billing system.");
    } else {
      System.out.println("Status :: " + message.getStatus());
      System.out.println("Msg    :: " + message.getMessage());
      if (!message.getStatus().equals("Success")) {
        throw new BillingException("deleteServiceInstance", "Error deleting ServiceInstance " + serviceinstance.getExternalId()
            + " to account " + account.getAccountno() + "..." + message.getMessage());
      }
    }
    System.out.println("Done Disconnecting Service on Account " + account.getAccountno() + " and ServiceInstance "
        + serviceinstance.getExternalId());
  }

  public Account getAccountByAccountNo(int account_no) throws BillingException {
    BillName billName = getBillName(account_no);
    CustAddress custAddress = getCustAddress(account_no);

    Account lAccount = new Account();
    lAccount.setAccountno(account_no);
    lAccount.setFirstname(billName.getFirstName());
    lAccount.setLastname(billName.getLastName());

    lAccount.setBalance(getBalance(account_no));

    lAccount.setContact_email(getEmail(account_no));
    lAccount.setContact_number(getContactNumber(account_no));

    lAccount.setContact_address1(custAddress.getAddress1());
    lAccount.setContact_address2(custAddress.getAddress2());
    lAccount.setContact_city(custAddress.getCity());
    lAccount.setContact_state(custAddress.getState());
    lAccount.setContact_zip(custAddress.getZip());
    lAccount.setPackageList(getPackageList(lAccount, null));
    lAccount.setServiceinstancelist(getServiceInstanceList(lAccount));
    return lAccount;
  }

  public static final Account getAccount(int accountNumber) {
    BillName billName = getBillName(accountNumber);
    CustAddress custAddress = getCustAddress(accountNumber);
    Account account = new Account();
    account.setAccountno(accountNumber);
    account.setFirstname(billName.getFirstName());
    account.setLastname(billName.getLastName());
    account.setBalance(getBalance(accountNumber));
    account.setContact_email(getEmail(accountNumber));
    account.setContact_number(getContactNumber(accountNumber));
    account.setContact_address1(custAddress.getAddress1());
    account.setContact_address2(custAddress.getAddress2());
    account.setContact_city(custAddress.getCity());
    account.setContact_state(custAddress.getState());
    account.setContact_zip(custAddress.getZip());
    account.setPackageList(getPackageList(account, null));
    account.setServiceinstancelist(getServiceInstanceList(account));
    return account;
  }

  public int getAccountNoByTN(String TN) {
    System.out.println("Get Account by TN :: " + TN);
    ValueHolder value = port.getAccountNo("usernmae", TN);
    if (value != null) {
      System.out.println("Status    :: " + value.getStatusMessage().getStatus());
      System.out.println("Message   :: " + value.getStatusMessage().getMessage());
      System.out.println("Value     :: " + value.getValue());
      System.out.println("Value2    :: " + value.getValue2());
      if (value.getValue() != null) {
        return Integer.parseInt(value.getValue());
      }
    }
    return 0;

  }

  private static String getBalance(int accountno) {
    String retValue = "0.00";
    CustBalanceHolder valueHolder = port.getCurrentBalance(USERNAME, Integer.toString(accountno));
    try {
      if (valueHolder != null) {
        // if( valueHolder.getCustBalance().getRealBalance()*-1 > 0 ) {
        DecimalFormat df = new DecimalFormat("0.00");
        // retValue =
        // df.getCurrency().getSymbol()+df.format((valueHolder.getCustBalance().getRealBalance()*-1));
        retValue = df.format((valueHolder.getCustBalance().getRealBalance() * -1));
        // }
      }
    } catch (WebServiceException ws_ex) {
      retValue = "0.00";
    } catch (NullPointerException np_ex) {
      retValue = "0.00";
    }
    return retValue;
  }

  private BillingAccount getBillingAccountDefault() throws BillingException {
    if (props == null) {
      throw new BillingException("getBillingAccountDefault",
          "Default file is not bound to application...account creation not possible");
    } else {

      BillingAccount billingAccount = new BillingAccount();

      billingAccount.setAccountCategory(Short.parseShort(props.getProperty("account.account_category")));
      billingAccount.setBillDispMethod(Short.parseShort(props.getProperty("account.bill_disp_meth")));
      billingAccount.setBillFormatOpt(Integer.parseInt(props.getProperty("account.bill_fmt_opt")));

      BillName billname = new BillName();
      billname.setFirstName("Shell");
      billname.setMiddleName("");
      billname.setLastName("Account");
      billingAccount.setBillName(billname);

      billingAccount.setBillPeriod(props.getProperty("account.bill_period"));
      billingAccount.setCCardIdServ(Short.parseShort(props.getProperty("account.default_ccard_id_serv")));
      billingAccount.setCollectionIndicator(Short.parseShort(props.getProperty("account.collection_indicator")));

      ContactInfo contactinfo = new ContactInfo();
      contactinfo.setContact1Name("");
      contactinfo.setContact1Phone("");
      billingAccount.setContactInfo(contactinfo);

      billingAccount.setCreditThresh(props.getProperty("account.credit_thresh"));
      billingAccount.setCredStatus(Short.parseShort(props.getProperty("account.cred_status")));
      billingAccount.setCurrencyCode(Short.parseShort(props.getProperty("account.currency_code")));

      CustAddress custAddress = new CustAddress();
      custAddress.setAddress1("355 S Grand Ave");
      custAddress.setAddress2("");
      custAddress.setAddress3("");
      custAddress.setCity("Los Angeles");
      custAddress.setCountryCode(Short.parseShort(props.getProperty("account.cust_country_code")));
      custAddress.setCounty("Los Angeles");
      custAddress.setFranchiseTaxCode(Short.parseShort(props.getProperty("account.cust_franchise_tax_code")));
      custAddress.setState("CA");
      custAddress.setZip("90071");
      billingAccount.setCustAddress(custAddress);

      // BillAddress billingAddress = new BillingAddress();
      billingAccount.setBillAddress(custAddress);

      billingAccount.setCustEmail("tscwebgeek@telscape.net");
      billingAccount.setCustFaxNo("");
      billingAccount.setCustPhone1("2133880022");
      billingAccount.setCustPhone2("");

      billingAccount.setExrateClass(Short.parseShort(props.getProperty("account.exrate_class")));
      billingAccount.setExternalAccountNoType(Short.parseShort(props.getProperty("account.account_type")));
      billingAccount.setInsertGrpId(Short.parseShort(props.getProperty("account.insert_grp_id")));

      billingAccount.setLanguageCode(Short.parseShort(props.getProperty("account.language_code")));
      billingAccount.setMarketCode(Short.parseShort(props.getProperty("account.mkt_code")));
      billingAccount.setMsgGroupId(Short.parseShort(props.getProperty("account.msg_grp_id")));

      billingAccount.setOwningCostCtr(Short.parseShort(props.getProperty("account.owning_cost_ctr")));
      billingAccount.setPaymentMethod(Short.parseShort(props.getProperty("account.pay_method")));

      billingAccount.setRateClassDefault(Short.parseShort(props.getProperty("account.rate_class_default")));
      /**
       * Double check this field
       */
      billingAccount.setServiceCenterId(Short.parseShort(props.getProperty("account.svc_ctr_id")));
      billingAccount.setServiceCenterType(Short.parseShort(props.getProperty("account.svc_ctr_type")));

      billingAccount.setSicCode(Short.parseShort(props.getProperty("account.sic_code")));
      billingAccount.setTieCode(Short.parseShort(props.getProperty("account.tie_code")));

      billingAccount.setVipCode(Short.parseShort(props.getProperty("account.vip_code")));
      try {
        XMLGregorianCalendar value = DatatypeFactory.newInstance().newXMLGregorianCalendar(
          new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles")));
        // DataTypeFactory.newXML
        billingAccount.setSysDate(value);
        billingAccount.setAccountDateActive(value);
      } catch (DatatypeConfigurationException dce) {
        dce.printStackTrace();
      }
      return billingAccount;
    }
  }

  private static BillName getBillName(int accountno) {
    BillName retValue = new BillName();

    BillNameHolder billNameHolder = port.getCustomerName(USERNAME, Integer.toString(accountno));
    if (billNameHolder != null) {
      return billNameHolder.getBillName();
    }

    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();

    Query q = session.getNamedQuery("get_cust_name");
    q.setParameter("in_username", USERNAME);
    q.setParameter("in_account_no", accountno);

    @SuppressWarnings("unchecked")
    List<BillName> billNameList = q.list();

    for (BillName billName : billNameList) {
      System.out.println("BillName :: " + billName.getFirstName() + " " + billName.getLastName());
      retValue = billName;
    }
    session.getTransaction().rollback();
    return retValue;
  }

  public List<PaymentHolder> getCompletePaymentHistory(Account account) {
    ArrayOfPaymentHolder paymentHolderList = port.getCompletePaymentHistory(USERNAME, Integer.toString(account.getAccountno()));
    return paymentHolderList.getPaymentHolder();
  }

  public List<Component> getComponentList(Account account, ServiceInstance serviceinstance, Package packageinstance)
      throws BillingException {
    if (account == null || account.getAccountno() <= 0) {
      throw new BillingException("getComponentList", "Account information not populated...");
    }
    if (serviceinstance == null || serviceinstance.getExternalId() == null
        || serviceinstance.getExternalId().trim().length() == 0) {
      throw new BillingException("getComponentList", "Service information not populated.");
    }
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();
    Query q = session.getNamedQuery("get_active_components");
    q.setParameter("in_username", USERNAME);
    q.setParameter("in_account_no", account.getAccountno());
    q.setParameter("in_external_id", serviceinstance.getExternalId());
    @SuppressWarnings("unchecked")
    List<Component> componentList = q.list();
    session.getTransaction().rollback();
    return componentList;
  }

  private static String getContactNumber(int accountno) {
    try {
      ContactInfoHolder contactInfo = port.getContactInfo(USERNAME, Integer.toString(accountno));
      return contactInfo.getContactInfo().getContact1Phone();
    } catch (NullPointerException npe) {
      // do nothing
    }
    return null;
  }

  private static CustAddress getCustAddress(int accountno) {
    CustAddress retValue = new CustAddress();

    BillingAddressHolder billAddressHolder = port.getBillingAddress(USERNAME, Integer.toString(accountno));
    if (billAddressHolder != null) {
      return billAddressHolder.getBillAddress();
    }

    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();

    Query q = session.getNamedQuery("get_billing_address");
    q.setParameter("in_username", USERNAME);
    q.setParameter("in_account_no", accountno);

    @SuppressWarnings("unchecked")
    List<CustAddress> custAddressList = q.list();
    for (CustAddress custAddress : custAddressList) {
      retValue = custAddress;
      StringBuffer sb = new StringBuffer();
      if (custAddress.getAddress1() != null) {
        sb.append(custAddress.getAddress1() + " ");
      }
      if (custAddress.getAddress2() != null) {
        sb.append(custAddress.getAddress2() + " ");
      }
      if (custAddress.getAddress3() != null) {
        sb.append(custAddress.getAddress3() + " ");
      }
      if (custAddress.getCity() != null) {
        sb.append(custAddress.getCity() + " ");
      }
      if (custAddress.getState() != null) {
        sb.append(custAddress.getState() + " ");
      }
      if (custAddress.getZip() != null) {
        sb.append(custAddress.getZip() + " ");
      }
      System.out.println("Billing Address :: " + sb.toString());
    }
    session.getTransaction().rollback();
    return retValue;
  }

  private static String getEmail(int acccountno) {
    ValueHolder valueHolder = port.getEmail(USERNAME, Integer.toString(acccountno));
    if (valueHolder != null) {
      return valueHolder.getValue();
    }
    return null;
  }

  public static List<Package> getPackageList(Account account, ServiceInstance serviceinstance) throws BillingException {
    if (account == null || account.getAccountno() <= 0) {
      throw new BillingException("getPackageList", "Account information not populated...");
    }
    try {
      ArrayOfPackageHolder arrayOfPackages = port.getListActivePackages(USERNAME, Integer.toString(account.getAccountno()));
      if (arrayOfPackages != null) {
        Vector<Package> packageList = new Vector<Package>();
        for (PackageHolder packageHolder : arrayOfPackages.getPackageHolder()) {
          Package tscpPackage = new Package();
          tscpPackage.setActiveDate(packageHolder.getPackage().getActiveDate().toGregorianCalendar().getTime());
          tscpPackage.setInactiveDate(packageHolder.getPackage().getDiscDate().toGregorianCalendar().getTime());
          tscpPackage.setInstanceId(packageHolder.getPackage().getPackageInstanceId());
          tscpPackage.setInstanceIdServ(Short.toString(packageHolder.getPackage().getPackageInstanceIdServ()));
          tscpPackage.setName(packageHolder.getPackage().getPackageName());
          tscpPackage.setId(packageHolder.getPackage().getPackageId());
          // tscpPackage.setComponentlist(getComponentList(account,null,tscpPackage));
          packageList.add(tscpPackage);
        }
        return packageList;
      }
    } catch (WebServiceException ws_ex) {
      System.out.println("WS Exception thrown when calling getListActivePackages(\"username\"," + account.getAccountno()
          + ")...." + ws_ex.getMessage());
      throw new BillingException("Error retrieving Package information:" + ws_ex.getMessage());
    }

    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();

    Query q = session.getNamedQuery("get_active_packages");
    q.setParameter("in_username", USERNAME);
    q.setParameter("in_account_no", account.getAccountno());

    @SuppressWarnings("unchecked")
    List<com.tscp.mvne.billing.Package> packageList = q.list();

    for (com.tscp.mvne.billing.Package acctPackage : packageList) {
      System.out.println("PackageId   :: " + acctPackage.getId());
      System.out.println("PackageName :: " + acctPackage.getName());
      System.out.println("PackageInstanceId :: " + acctPackage.getInstanceId());
    }

    session.getTransaction().rollback();
    return packageList;
  }

  public static List<ServiceInstance> getServiceInstanceList(Account account) throws BillingException {
    if (account == null || account.getAccountno() <= 0) {
      throw new BillingException("getServiceInstanceList", "Account information must be populated.");
    }
    try {
      ArrayOfServiceHolder serviceHolderList = port.getActiveService(USERNAME, Integer.toString(account.getAccountno()));
      if (serviceHolderList != null) {
        Vector<ServiceInstance> serviceInstanceList = new Vector<ServiceInstance>();
        for (ServiceHolder serviceHolder : serviceHolderList.getServiceHolder()) {
          ServiceInstance serviceInstance = new ServiceInstance();
          serviceInstance.setExternalId(serviceHolder.getService().getExternalId());
          serviceInstance.setExternalIdType(serviceHolder.getService().getExternalIdType());
          serviceInstance.setSubscriberNumber(Integer.parseInt(serviceHolder.getService().getSubscrNo()));
          serviceInstanceList.add(serviceInstance);
        }
        return serviceInstanceList;
      }
    } catch (WebServiceException ws_ex) {
      System.out.println("WS Exception thrown when calling getActiveService(\"username\"," + account.getAccountno() + ")...."
          + ws_ex.getMessage());
      throw new BillingException("Error retrieving Service Instance information:" + ws_ex.getMessage());
    }

    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();

    Query q = session.getNamedQuery("get_active_services");
    q.setParameter("in_username", USERNAME);
    q.setParameter("in_account_no", account.getAccountno());

    @SuppressWarnings("unchecked")
    List<ServiceInstance> serviceInstanceList = (List<ServiceInstance>) q.list();

    for (ServiceInstance si : serviceInstanceList) {
      System.out.println("ExternalId  :: " + si.getExternalId());
      System.out.println("SubscrNo    :: " + si.getSubscriberNumber());
    }

    session.getTransaction().rollback();
    return serviceInstanceList;
  }

  public UsageHolder getUnbilledUsageSummary(ServiceInstance serviceInstance) {
    UsageHolder usageHolder = port.getUnbilledDataMBs(USERNAME, serviceInstance.getExternalId());
    return usageHolder;
  }

  private XMLGregorianCalendar sysdate() {
    try {
      XMLGregorianCalendar sysdate = DatatypeFactory.newInstance().newXMLGregorianCalendar(
        new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles")));
      return sysdate;
    } catch (DatatypeConfigurationException dce) {
      dce.printStackTrace();
    }
    return null;
  }

  public void updateAccount(Account account) throws BillingException {
    if (account == null || account.getAccountno() <= 0) {
      throw new BillingException("updateAccount", "Please Specify and account to update...");
    }
    if (account.getContact_email() != null) {
      MessageHolder message = port.updateEmail(USERNAME, Integer.toString(account.getAccountno()), account.getContact_email());
      if (message == null) {
        throw new BillingException("updateAccount", "Error updating account " + account.getAccountno()
            + "...No response returned from billing system.");
      } else {
        if (!message.getStatus().equals("Success")) {
          throw new BillingException("updateAccount", "Error updating email address for account " + account.getAccountno()
              + ". Returned message is " + message.getMessage());
        }
      }
    }
  }

  public void updateAccountEmailAddress(Account account) {
    if (account.getAccountno() <= 0) {
      throw new BillingException("Account number must be populated.");
    }
    if (account.getContact_email() == null || account.getContact_email().trim().length() == 0) {
      throw new BillingException("Email address cannot be empty.");
    }
    MessageHolder messageHolder = port.updateEmail("system", Integer.toString(account.getAccountno()), account
        .getContact_email());
    if (messageHolder != null) {
      System.out.println("Status    :: " + messageHolder.getStatus());
      System.out.println("Message   :: " + messageHolder.getMessage());
    } else {
      throw new BillingException("No response from billing system.");
    }
  }

  public void updateServiceInstanceStatus(ServiceInstance serviceInstance, int newThreshold) throws BillingException {
    if (serviceInstance == null || serviceInstance.getExternalId() == null
        || serviceInstance.getExternalId().trim().length() == 0) {
      throw new BillingException("Valid service instance required");
    }
    if (serviceInstance.getExternalIdType() == 0) {
      throw new BillingException("Invalid External ID Type value...");
    }
    if (newThreshold != 0 || newThreshold != 5 || newThreshold != 7) {
      throw new BillingException("Invalid Threshold Value");
    }
    MessageHolder messageHolder = port.updateThreshold(USERNAME, serviceInstance.getExternalId(), serviceInstance
        .getExternalIdType(), Integer.toString(newThreshold));
    if (messageHolder == null) {
      throw new BillingException("No response from billing system");
    } else {
      if (!messageHolder.getStatus().equals("Success")) {
        throw new BillingException("Billing System error: " + messageHolder.getMessage());
      } else {
        System.out.println("Service " + serviceInstance.getExternalId() + " with external_id_type "
            + serviceInstance.getExternalIdType() + " has been updated with new threshold value of " + newThreshold);
      }
    }
  }
}