package com.tscp.mvne.billing.contract;

import java.util.Date;
import java.util.List;

import com.tscp.mvne.billing.Account;
import com.tscp.mvne.billing.contract.dao.KenanContractDao;
import com.tscp.mvne.billing.contract.exception.ContractException;
import com.tscp.mvne.billing.provisioning.ServiceInstance;

public class ContractService {

  public int applyContract(KenanContract contract) throws ContractException {
    try {
      contract.validate();
      return KenanContractDao.insertContract(contract);
    } catch (ContractException e) {
      throw e;
    }

  }

  public void updateContract(KenanContract contract) throws ContractException {
    try {
      contract.validate();
      KenanContractDao.updateContract(contract);
    } catch (ContractException e) {
      throw e;
    }
  }

  public List<KenanContract> getContracts(Account account, ServiceInstance serviceInstance) throws ContractException {
    return KenanContractDao.getContracts(account, serviceInstance);
  }

  public int applyCouponPayment(Account account, String amount, Date date) throws ContractException {
    return KenanContractDao.applyCouponPayment(account, amount, date);
  }
}
