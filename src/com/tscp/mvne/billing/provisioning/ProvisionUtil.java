package com.tscp.mvne.billing.provisioning;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.tscp.mvne.billing.Component;
import com.tscp.mvne.billing.Package;
import com.tscp.mvne.billing.ServiceInstance;
import com.tscp.mvne.billing.api.ArrayOfMessageHolder;
import com.tscp.mvne.billing.api.ArrayOfPackage;
import com.tscp.mvne.billing.api.ArrayOfPkgComponent;
import com.tscp.mvne.billing.api.MessageHolder;
import com.tscp.mvne.billing.api.PkgComponent;
import com.tscp.mvne.billing.api.Service;
import com.tscp.mvne.billing.exception.NoResponseException;
import com.tscp.mvne.billing.exception.ProvisionException;
import com.tscp.mvne.config.ProvisionConfig;

/**
 * Validation utility for provisioning serviceInstances, packages and components
 * 
 * @author Tachikoma
 * 
 */
public final class ProvisionUtil {
  public static final String SUCCESS = "SUCCESS";
  public static final String FAIL = "FAIL";

  /**
   * Returns a Package as required by the Billing Server or the default package
   * if none is specified.
   * 
   * @param pkg
   * @return
   */
  public static final com.tscp.mvne.billing.api.Package buildBillingPackage(Package pkg) {
    com.tscp.mvne.billing.api.Package billingPackage;
    if (pkg == null) {
      billingPackage = getDefaultBillingPackage();
    } else {
      billingPackage = new com.tscp.mvne.billing.api.Package();
      billingPackage.setAccountNo(Integer.toString(pkg.getAccountNumber()));
      billingPackage.setPackageId(pkg.getId());
      billingPackage.setPackageInstanceId(pkg.getInstanceId());
      billingPackage.setPackageInstanceIdServ(Short.parseShort(pkg.getInstanceIdServ()));
      billingPackage.setPackageName(pkg.getName());
      billingPackage.setActiveDate(getCalendar(pkg.getActiveDate()));
      billingPackage.setDiscDate(getCalendar(pkg.getInactiveDate()));
    }
    return billingPackage;
  }

  public static final Component buildComponent(PkgComponent billingComponent) {
    Component component = new Component();
    component.setActiveDate(billingComponent.getComponentActiveDate().toGregorianCalendar().getTime());
    component.setId(billingComponent.getComponentId());
    component.setInactiveDate(billingComponent.getDiscDate().toGregorianCalendar().getTime());
    component.setInstanceId(billingComponent.getComponentInstanceId());
    component.setName(billingComponent.getComponentName());
    return component;
  }

  public static final ArrayOfPkgComponent buildComponentList(String externalId, Package pkg, Component component) {
    PkgComponent pkgComponent = ProvisionUtil.buildPkgComponent(externalId, pkg, component);
    ArrayOfPkgComponent componentList = new ArrayOfPkgComponent();
    componentList.getPkgComponent().add(pkgComponent);
    return componentList;
  }

  public static final ArrayOfPackage buildPackageList(Package pkg) {
    com.tscp.mvne.billing.api.Package billingPackage = ProvisionUtil.buildBillingPackage(pkg);
    ArrayOfPackage packageList = new ArrayOfPackage();
    packageList.getPackage().add(billingPackage);
    return packageList;
  }

  public static final Package buildPackage(com.tscp.mvne.billing.api.Package billingPackage) {
    Package pkg = new Package();
    if (billingPackage.getAccountNo() != null && !billingPackage.getAccountNo().isEmpty()) {
      pkg.setAccountNumber(Integer.parseInt(billingPackage.getAccountNo()));
    }
    pkg.setActiveDate(billingPackage.getActiveDate().toGregorianCalendar().getTime());
    pkg.setInactiveDate(billingPackage.getDiscDate().toGregorianCalendar().getTime());
    pkg.setInstanceId(billingPackage.getPackageInstanceId());
    pkg.setInstanceIdServ(Short.toString(billingPackage.getPackageInstanceIdServ()));
    pkg.setName(billingPackage.getPackageName());
    pkg.setId(billingPackage.getPackageId());
    return pkg;
  }

  public static final PkgComponent buildPkgComponent(String externalId, Package pkg, Component component) {
    PkgComponent pkgComponent = ProvisionUtil.getDefaultBillingComponent();
    pkgComponent.setExternalId(externalId);
    if (component == null) {
      component = new Component();
    } else if (component.getId() > 0) {
      pkgComponent.setComponentId(component.getId());
    } else {
      component.setId(pkgComponent.getComponentId());
    }
    if (component.getInstanceId() > 0) {
      pkgComponent.setComponentInstanceId(component.getInstanceId());
    }
    if (pkg != null) {
      pkgComponent.setPackageInstanceId(pkg.getInstanceId());
      pkgComponent.setPackageInstanceIdServ(Short.parseShort(pkg.getInstanceIdServ()));
      if (pkg.getId() > 0) {
        pkgComponent.setPackageId(pkg.getId());
      }
    }
    return pkgComponent;
  }

  public static final Service buildService(int accountNumber, ServiceInstance serviceInstance) {
    Service service = new Service();
    service.setAccountNo(Integer.toString(accountNumber));
    service.setActiveDate(toServiceDate(serviceInstance.getActiveDate()));
    service.setExternalId(serviceInstance.getExternalId());
    service.setExternalIdType(serviceInstance.getExternalIdType());
    service.setInactiveDate(toServiceDate(serviceInstance.getInactiveDate()));
    service.setSubscrNo(Integer.toString(serviceInstance.getSubscriberNumber()));
    return service;
  }

  public static final ServiceInstance buildServiceInstance(Service service) {
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setActiveDate(getServiceDate(service.getActiveDate()));
    serviceInstance.setExternalId(service.getExternalId());
    serviceInstance.setExternalIdType(service.getExternalIdType());
    serviceInstance.setInactiveDate(getServiceDate(service.getInactiveDate()));
    serviceInstance.setSubscriberNumber(Integer.parseInt(service.getSubscrNo()));
    return serviceInstance;
  }

  public static final void checkAccountNumber(int accountNumber) throws ProvisionException {
    if (accountNumber == 0) {
      throw new ProvisionException("Account Number is not set");
    }
  }

  public static final void checkExternalId(String externalId) throws ProvisionException {
    if (externalId == null || externalId.trim().isEmpty()) {
      throw new ProvisionException("External ID is not set");
    }
  }

  public static final void checkPackage(Package pkg) throws ProvisionException {
    if (pkg == null || pkg.getInstanceId() == 0) {
      throw new ProvisionException("Package is not set");
    }
  }

  public static final XMLGregorianCalendar getCalendar() {
    return getCalendar(new Date());
  }

  public static final XMLGregorianCalendar getCalendar(Date date) {
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(date);
      XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
      return xmlDate;
    } catch (DatatypeConfigurationException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static final PkgComponent getDefaultBillingComponent() {
    int componentid = Integer.parseInt(ProvisionConfig.Component.installId);
    int packageid = Integer.parseInt(ProvisionConfig.Component.packageId);
    short externalidtype = Short.parseShort(ProvisionConfig.Component.externalIdType);
    short componentInstanceIdServ = Short.parseShort(ProvisionConfig.Component.instanceServId);
    short packageInstanceIdServ = Short.parseShort(ProvisionConfig.Package.instanceServId);
    short packageInstanceId = Short.parseShort(ProvisionConfig.Package.instanceId);
    PkgComponent pkgComponent = new PkgComponent();
    pkgComponent.setComponentId(componentid);
    pkgComponent.setPackageInstanceId(packageInstanceId);
    pkgComponent.setPackageInstanceIdServ(packageInstanceIdServ);
    pkgComponent.setComponentActiveDate(getCalendar());
    pkgComponent.setExternalId("");
    pkgComponent.setExternalIdType(externalidtype);
    pkgComponent.setPackageId(packageid);
    pkgComponent.setComponentInstanceIdServ(componentInstanceIdServ);
    return pkgComponent;
  }

  public static final com.tscp.mvne.billing.api.Package getDefaultBillingPackage() {
    int packageId = Integer.parseInt(ProvisionConfig.Package.id);
    short externalIdType = Short.parseShort(ProvisionConfig.Package.externalIdType);
    com.tscp.mvne.billing.api.Package billingPackage = new com.tscp.mvne.billing.api.Package();
    billingPackage.setPackageId(packageId);
    billingPackage.setExternalIdType(externalIdType);
    billingPackage.setActiveDate(getCalendar());
    billingPackage.setAccountNo("");
    return billingPackage;
  }

  public static final com.tscp.mvne.billing.api.Package getDefaultBillingPackage(int accountNumber) {
    com.tscp.mvne.billing.api.Package billingPackage = getDefaultBillingPackage();
    billingPackage.setAccountNo(Integer.toString(accountNumber));
    return billingPackage;
  }

  public static final String getResponse(ArrayOfMessageHolder messageHolder) throws ProvisionException {
    if (messageHolder != null && messageHolder.getMessageHolder() != null && !messageHolder.getMessageHolder().isEmpty()) {
      MessageHolder message = messageHolder.getMessageHolder().get(0);
      if (message.getStatus().trim().equalsIgnoreCase("SUCCESS")) {
        return "SUCCESS";
      } else {
        return message.getMessage();
      }
    } else {
      throw new NoResponseException("addComponent", "No response from billing system");
    }
  }

  public static final Date getServiceDate(String serviceDate) {
    if (!serviceDate.trim().isEmpty()) {
      DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy kk:mm:ss aa");
      DateTime dateTime = fmt.parseDateTime(serviceDate.trim());
      return dateTime.toDate();
    } else {
      return null;
    }
  }

  @Deprecated
  public static final void syncPackages(Package pkg, com.tscp.mvne.billing.api.Package billingPackage) {
    if (pkg.getId() > 0) {
      billingPackage.setPackageId(pkg.getId());
    } else {
      pkg.setId(billingPackage.getPackageId());
    }
  }

  public static final String toServiceDate(Date date) {
    DateTime serviceDate = new DateTime(date);
    DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy kk:mm:ss aa");
    return serviceDate.toString(fmt);
  }

}
