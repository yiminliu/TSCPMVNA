package com.tscp.mvne.billing.contract;

import com.tscp.mvne.billing.Account;
import com.tscp.mvne.billing.contract.exception.ContractException;
import com.tscp.mvne.billing.provisioning.ServiceInstance;

public class KenanContract {
  private Account account;
  private ServiceInstance serviceInstance;
  private int contractType;
  private int contractId;
  private int duration;
  private String description;

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public ServiceInstance getServiceInstance() {
    return serviceInstance;
  }

  public void setServiceInstance(ServiceInstance serviceInstance) {
    this.serviceInstance = serviceInstance;
  }

  public int getContractType() {
    return contractType;
  }

  public void setContractType(int contractType) {
    this.contractType = contractType;
  }

  public int getContractId() {
    return contractId;
  }

  public void setContractId(int contractId) {
    this.contractId = contractId;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  // TODO validation should be moved out of the object and into a validator
  // class
  public void validate() throws ContractException {
    validateAccount();
    validateServiceInstance();
    validateContractType();
    validateDuration();
  }

  private void validateAccount() throws ContractException {
    if (account == null || account.getAccountno() == 0) {
      throw new ContractException("Account is not set");
    }
  }

  private void validateServiceInstance() throws ContractException {
    if (serviceInstance == null || serviceInstance.getExternalId() == null || serviceInstance.getExternalId().isEmpty()) {
      throw new ContractException("ServiceInstance is not set");
    }
  }

  private void validateContractType() throws ContractException {
    if (contractType == 0) {
      throw new ContractException("ContractType is not set");
    }
  }

  private void validateDuration() throws ContractException {
    if (duration < 0) {
      throw new ContractException("Duration is not set");
    }
  }

  @Override
  public String toString() {
    return "KenanContract [account=" + account + ", serviceInstance=" + serviceInstance + ", contractType=" + contractType + ", contractId=" + contractId
        + ", duration=" + duration + ", description=" + description + "]";
  }

}