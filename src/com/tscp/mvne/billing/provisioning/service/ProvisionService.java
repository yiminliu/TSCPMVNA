package com.tscp.mvne.billing.provisioning.service;

import java.util.List;

import org.joda.time.DateTime;

import com.tscp.mvne.billing.exception.ProvisionException;
import com.tscp.mvne.billing.provisioning.Component;
import com.tscp.mvne.billing.provisioning.Package;
import com.tscp.mvne.billing.provisioning.ProvisionSystem;
import com.tscp.mvne.billing.provisioning.ProvisionSystemProvider;
import com.tscp.mvne.billing.provisioning.ServiceInstance;
import com.tscp.mvne.config.PROVISION;

public class ProvisionService {
  private static final ProvisionSystem system = ProvisionSystemProvider.getInstance();

  protected void installService(int accountNo, String externalId, boolean reinstall) throws ProvisionException {
    addServiceInstance(accountNo, externalId);
    Package pkg = addPackage(accountNo, null);
    if (reinstall) {
      addComponent(accountNo, externalId, pkg.getInstanceId(), PROVISION.COMPONENT.REINSTALL);
    } else {
      addComponent(accountNo, externalId, pkg.getInstanceId(), PROVISION.COMPONENT.INSTALL);
    }
  }

  public void installService(int accountNo, String externalId) throws ProvisionException {
    installService(accountNo, externalId, false);
  }

  public void reinstallService(int accountNo, String externalId) throws ProvisionException {
    installService(accountNo, externalId, true);
  }

  public void disconnectService(int accountNo, String externalId) throws ProvisionException {
    system.removeServiceInstance(accountNo, externalId);
  }

  public void addComponent(int accountNo, String externalId, int packageInstance, int componentId) throws ProvisionException {
    Component component = new Component(componentId);
    Package pkg;
    if (packageInstance == 0) {
      pkg = getActivePackage(accountNo);
    } else {
      pkg = getActivePackage(accountNo, packageInstance);
    }
    system.addComponent(accountNo, externalId, pkg, component);
  }

  public void addComponentFuture(int accountNo, String externalId, int packageInstance, int componentId) throws ProvisionException {
    Component component = new Component(componentId);
    Package pkg;
    if (packageInstance == 0) {
      pkg = getActivePackage(accountNo);
    } else {
      pkg = getActivePackage(accountNo, packageInstance);
    }
    system.addComponentFuture(accountNo, externalId, pkg, component);
  }

  public Package addPackage(int accountNo, Package pkg) throws ProvisionException {
    return system.addPackage(accountNo, pkg);
  }

  public void addServiceInstance(int accountNo, String externalId) throws ProvisionException {
    system.addServiceInstance(accountNo, externalId);
  }

  public void addSingleComponent(int accountNo, String externalId, int packageInstance, int componentId, DateTime activeDate) throws ProvisionException {
    Component component = new Component(componentId);
    Package pkg;
    if (packageInstance == 0) {
      pkg = getActivePackage(accountNo);
    } else {
      pkg = getActivePackage(accountNo, packageInstance);
    }
    system.addSingleComponent(accountNo, externalId, pkg, component, activeDate);
  }

  public void addSingleComponentToday(int accountNo, String externalId, int packageInstance, int componentId) throws ProvisionException {
    Component component = new Component(componentId);
    Package pkg;
    if (packageInstance == 0) {
      pkg = getActivePackage(accountNo);
    } else {
      pkg = getActivePackage(accountNo, packageInstance);
    }
    system.addSingleComponent(accountNo, externalId, pkg, component, new DateTime());
  }

  public void addSingleComponentNextDay(int accountNo, String externalId, int packageInstance, int componentId) throws ProvisionException {
    Component component = new Component(componentId);
    DateTime tommorrow = new DateTime().plusDays(1);
    component.setActiveDate(tommorrow);
    Package pkg;
    if (packageInstance == 0) {
      pkg = getActivePackage(accountNo);
    } else {
      pkg = getActivePackage(accountNo, packageInstance);
    }
    system.addSingleComponent(accountNo, externalId, pkg, component, new DateTime().plusDays(1));
  }

  public Component getActiveComponent(int accountNo, String externalId) throws ProvisionException {
    List<Component> components = getActiveComponents(accountNo, externalId);
    if (components == null || components.isEmpty()) {
      throw new ProvisionException("Account " + accountNo + " returned no active component for MDN " + externalId);
    } else if (components.size() > 1) {
      throw new ProvisionException("Account " + accountNo + " has more than one active component for MDN " + externalId);
    } else {
      return components.get(0);
    }
  }

  public List<Component> getActiveComponents(int accountNo, String externalId) throws ProvisionException {
    return system.getActiveComponents(accountNo, externalId);
  }

  public Package getActivePackage(int accountNo) throws ProvisionException {
    List<Package> packages = getActivePackages(accountNo);
    if (packages == null || packages.isEmpty()) {
      throw new ProvisionException("Account " + accountNo + " has more than no active package");
    } else if (packages.size() > 1) {
      throw new ProvisionException("Account " + accountNo + " has more than one active package");
    } else {
      return packages.get(0);
    }
  }

  public Package getActivePackage(int accountNo, int packageInstance) throws ProvisionException {
    List<Package> packages = getActivePackages(accountNo);
    if (packages != null) {
      for (Package pkg : packages) {
        if (pkg.getInstanceId() == packageInstance) {
          return pkg;
        }
      }
      throw new ProvisionException("Package " + packageInstance + " not found on account " + accountNo);
    } else {
      throw new ProvisionException("No active packages found for account " + accountNo);
    }
  }

  public List<Package> getActivePackages(int accountNo) throws ProvisionException {
    return system.getActivePackages(accountNo);
  }

  public ServiceInstance getActiveService(int accountNo) throws ProvisionException {
    List<ServiceInstance> services = getActiveServices(accountNo);
    if (services == null || services.isEmpty()) {
      throw new ProvisionException("Account " + accountNo + " has no active service");
    } else if (services.size() > 1) {
      throw new ProvisionException("Account " + accountNo + " has more than one active service");
    } else {
      return services.get(0);
    }
  }

  public List<ServiceInstance> getActiveServices(int accountNo) throws ProvisionException {
    return system.getActiveServices(accountNo);
  }

  public void removeComponent(int accountNo, String externalId, int packageInstance, int componentInstance, DateTime dateTime) throws ProvisionException {
    Package pkg;
    Component component;
    if (componentInstance == 0) {
      component = getActiveComponent(accountNo, externalId);
    } else {
      component = new Component();
      component.setInstanceId(componentInstance);
    }
    if (packageInstance == 0) {
      pkg = getActivePackage(accountNo);
    } else {
      pkg = getActivePackage(accountNo, packageInstance);
    }
    system.removeComponent(accountNo, externalId, pkg, component, dateTime);
  }

  public void removeComponentToday(int accountNo, String externalId, int packageInstance, int componentInstance) throws ProvisionException {
    removeComponent(accountNo, externalId, packageInstance, componentInstance, new DateTime());
  }

  public void removeComponentNextDay(int accountNo, String externalId, int packageInstance, int componentInstance) throws ProvisionException {
    removeComponent(accountNo, externalId, packageInstance, componentInstance, new DateTime().plusDays(1));
  }

  /**
   * This method is protected because the ability to remove packages is not
   * needed by the application.
   * 
   * @param accountNo
   * @param packageInstance
   * @throws ProvisionException
   */
  protected void removePackage(int accountNo, int packageInstance) throws ProvisionException {
    Package pkg;
    if (packageInstance == 0) {
      pkg = getActivePackage(accountNo);
    } else {
      pkg = getActivePackage(accountNo, packageInstance);
    }
    // system.removePackage(accountNo, pkg);
  }

}