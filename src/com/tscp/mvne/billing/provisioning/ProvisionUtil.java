package com.tscp.mvne.billing.provisioning;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.telscape.billingserviceinterface.ArrayOfPackage;
import com.telscape.billingserviceinterface.ArrayOfPkgComponent;
import com.telscape.billingserviceinterface.PkgComponent;
import com.telscape.billingserviceinterface.Service;
import com.tscp.mvne.billing.BillingUtil;
import com.tscp.mvne.billing.exception.ProvisionException;
import com.tscp.mvne.billing.provisioning.defaults.DefaultBillingComponent;
import com.tscp.mvne.billing.provisioning.defaults.DefaultBillingPackage;
import com.tscp.util.DateUtils;

/**
 * Validation utility for provisioning serviceInstances, packages and components
 * 
 * @author Tachikoma
 * 
 */
public final class ProvisionUtil extends BillingUtil {
  public static final String SUCCESS = "SUCCESS";
  public static final String FAIL = "FAIL";

  public static final boolean isCurrentMonth(Component component) {
    DateTime today = new DateTime();
    return component.getActiveDate().getYear() == today.getYear() && component.getActiveDate().getMonthOfYear() == today.getMonthOfYear();
  }

  /**
   * Returns a Package as required by the Billing Server or the default package
   * if none is specified.
   * 
   * @param pkg
   * @return
   */
  public static final com.telscape.billingserviceinterface.Package buildBillingPackage(int accountNo, Package pkg) {
    com.telscape.billingserviceinterface.Package billingPackage;
    if (pkg == null) {
      billingPackage = new DefaultBillingPackage(accountNo);
    } else {
      billingPackage = new com.telscape.billingserviceinterface.Package();
      billingPackage.setAccountNo(Integer.toString(pkg.getAccountNumber()));
      billingPackage.setPackageId(pkg.getId());
      billingPackage.setPackageInstanceId(pkg.getInstanceId());
      billingPackage.setPackageInstanceIdServ(((Integer) pkg.getInstanceIdServ()).shortValue());
      billingPackage.setPackageName(pkg.getName());
      billingPackage.setActiveDate(DateUtils.getXMLCalendar(pkg.getActiveDate()));
      billingPackage.setDiscDate(DateUtils.getXMLCalendar(pkg.getInactiveDate()));
    }
    return billingPackage;
  }

  public static final Component buildComponent(PkgComponent billingComponent) {
    DateTime activeDate = null, inactiveDate = null;
    if (billingComponent.getComponentActiveDate() != null)
      activeDate = new DateTime(billingComponent.getComponentActiveDate().toGregorianCalendar());
    if (billingComponent.getDiscDate() != null)
      inactiveDate = new DateTime(billingComponent.getDiscDate().toGregorianCalendar());
    Component component = new Component();
    component.setActiveDate(activeDate);
    component.setId(billingComponent.getComponentId());
    component.setInactiveDate(inactiveDate);
    component.setInstanceId(billingComponent.getComponentInstanceId());
    component.setName(billingComponent.getComponentName());
    component.setPackageInstanceId(billingComponent.getPackageInstanceId());
    component.setExternalId(billingComponent.getExternalId());
    return component;
  }

  public static final ArrayOfPkgComponent buildComponentList(String externalId, Package pkg, Component component) {
    PkgComponent pkgComponent = ProvisionUtil.buildPkgComponent(externalId, pkg, component);
    ArrayOfPkgComponent componentList = new ArrayOfPkgComponent();
    componentList.getPkgComponent().add(pkgComponent);
    return componentList;
  }

  public static final ArrayOfPackage buildPackageList(int accountNo, Package pkg) {
    com.telscape.billingserviceinterface.Package billingPackage = ProvisionUtil.buildBillingPackage(accountNo, pkg);
    ArrayOfPackage packageList = new ArrayOfPackage();
    packageList.getPackage().add(billingPackage);
    return packageList;
  }

  public static final Package buildPackage(com.telscape.billingserviceinterface.Package billingPackage) {
    Package pkg = new Package();
    if (billingPackage.getAccountNo() != null && !billingPackage.getAccountNo().isEmpty()) {
      pkg.setAccountNumber(Integer.parseInt(billingPackage.getAccountNo()));
    }
    pkg.setActiveDate(billingPackage.getActiveDate().toGregorianCalendar().getTime());
    pkg.setInactiveDate(billingPackage.getDiscDate().toGregorianCalendar().getTime());
    pkg.setInstanceId(billingPackage.getPackageInstanceId());
    pkg.setInstanceIdServ(billingPackage.getPackageInstanceIdServ());
    pkg.setName(billingPackage.getPackageName());
    pkg.setId(billingPackage.getPackageId());
    return pkg;
  }

  public static final PkgComponent buildPkgComponent(String externalId, Package pkg, Component component) {
    PkgComponent pkgComponent = new DefaultBillingComponent();
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
    if (component.getActiveDate() != null) {
      pkgComponent.setComponentActiveDate(DateUtils.getXMLCalendar(component.getActiveDate()));
    }
    if (component.getInactiveDate() != null) {
      pkgComponent.setDiscDate(DateUtils.getXMLCalendar(component.getInactiveDate()));
    }
    if (pkg != null) {
      pkgComponent.setPackageInstanceId(pkg.getInstanceId());
      pkgComponent.setPackageInstanceIdServ(((Integer) pkg.getInstanceIdServ()).shortValue());
      if (pkg.getId() > 0) {
        pkgComponent.setPackageId(pkg.getId());
      }
    }
    return pkgComponent;
  }

  public static final Service buildService(int accountNo, ServiceInstance serviceInstance) {
    Service service = new Service();
    service.setAccountNo(Integer.toString(accountNo));
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

  public static final void checkComponent(Component component) throws ProvisionException {
    if (component.getId() == 0) {
      throw new ProvisionException("Component ID is not set");
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
  public static final void syncPackages(Package pkg, com.telscape.billingserviceinterface.Package billingPackage) {
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
