package com.tscp.mvne.billing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import com.tscp.mvne.billing.provisioning.Package;
import com.tscp.mvne.billing.provisioning.ServiceInstance;

// TODO jpong: Move address to a separate embedded class. This will affect TruConnect webfont as well.
public class Account implements Serializable {
  private static final long serialVersionUID = 1L;
  private int accountCategory;
  private int accountNo;
  private Date activeDate;
  private String balance;
  private String contact_address1;
  private String contact_address2;
  private String contact_city;
  private String contact_email;
  private String contact_number;
  private String contact_state;
  private String contact_zip;
  private String firstName;
  private Date inactiveDate;
  private String lastName;
  private String middleName;
  private Collection<Package> packageList;
  private Collection<ServiceInstance> serviceInstanceList;

  public int getAccountNo() {
    return accountNo;
  }

  public int getAccount_category() {
    return accountCategory;
  }

  public Date getActive_date() {
    return activeDate;
  }

  public String getBalance() {
    return balance;
  }

  public String getContact_address1() {
    return contact_address1;
  }

  public String getContact_address2() {
    return contact_address2;
  }

  public String getContact_city() {
    return contact_city;
  }

  public String getContact_email() {
    return contact_email;
  }

  public String getContact_number() {
    return contact_number;
  }

  public String getContact_state() {
    return contact_state;
  }

  public String getContact_zip() {
    return contact_zip;
  }

  public String getFirstname() {
    return firstName;
  }

  public Date getInactive_date() {
    return inactiveDate;
  }

  public String getLastname() {
    return lastName;
  }

  public String getMiddlename() {
    return middleName;
  }

  public Collection<Package> getPackageList() {
    return packageList;
  }

  public Collection<ServiceInstance> getServiceinstancelist() {
    return serviceInstanceList;
  }

  public void setAccount_category(int account_category) {
    this.accountCategory = account_category;
  }

  public void setAccountNo(int accountNo) {
    this.accountNo = accountNo;
  }

  public void setActive_date(Date active_date) {
    this.activeDate = active_date;
  }

  public void setBalance(String balance) {
    this.balance = balance;
  }

  public void setContact_address1(String contact_address1) {
    this.contact_address1 = contact_address1;
  }

  public void setContact_address2(String contact_address2) {
    this.contact_address2 = contact_address2;
  }

  public void setContact_city(String contact_city) {
    this.contact_city = contact_city;
  }

  public void setContact_email(String contact_email) {
    this.contact_email = contact_email;
  }

  public void setContact_number(String contact_number) {
    this.contact_number = contact_number;
  }

  public void setContact_state(String contact_state) {
    this.contact_state = contact_state;
  }

  public void setContact_zip(String contact_zip) {
    this.contact_zip = contact_zip;
  }

  public void setFirstname(String firstname) {
    this.firstName = firstname;
  }

  public void setInactive_date(Date inactive_date) {
    this.inactiveDate = inactive_date;
  }

  public void setLastname(String lastname) {
    this.lastName = lastname;
  }

  public void setMiddlename(String middlename) {
    this.middleName = middlename;
  }

  public void setPackageList(Collection<Package> packageList) {
    this.packageList = packageList;
  }

  public void setServiceinstancelist(Collection<ServiceInstance> serviceinstancelist) {
    this.serviceInstanceList = serviceinstancelist;
  }

  @Override
  public String toString() {
    return "Account [accountCategory=" + accountCategory + ", accountNo=" + accountNo + ", activeDate=" + activeDate + ", balance=" + balance
        + ", contact_address1=" + contact_address1 + ", contact_address2=" + contact_address2 + ", contact_city=" + contact_city + ", contact_email="
        + contact_email + ", contact_number=" + contact_number + ", contact_state=" + contact_state + ", contact_zip=" + contact_zip + ", firstName="
        + firstName + ", inactiveDate=" + inactiveDate + ", lastName=" + lastName + ", middleName=" + middleName + ", packageList=" + packageList
        + ", serviceInstanceList=" + serviceInstanceList + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + accountCategory;
    result = prime * result + accountNo;
    result = prime * result + ((activeDate == null) ? 0 : activeDate.hashCode());
    result = prime * result + ((balance == null) ? 0 : balance.hashCode());
    result = prime * result + ((contact_address1 == null) ? 0 : contact_address1.hashCode());
    result = prime * result + ((contact_address2 == null) ? 0 : contact_address2.hashCode());
    result = prime * result + ((contact_city == null) ? 0 : contact_city.hashCode());
    result = prime * result + ((contact_email == null) ? 0 : contact_email.hashCode());
    result = prime * result + ((contact_number == null) ? 0 : contact_number.hashCode());
    result = prime * result + ((contact_state == null) ? 0 : contact_state.hashCode());
    result = prime * result + ((contact_zip == null) ? 0 : contact_zip.hashCode());
    result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
    result = prime * result + ((inactiveDate == null) ? 0 : inactiveDate.hashCode());
    result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
    result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
    result = prime * result + ((packageList == null) ? 0 : packageList.hashCode());
    result = prime * result + ((serviceInstanceList == null) ? 0 : serviceInstanceList.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Account other = (Account) obj;
    if (accountCategory != other.accountCategory)
      return false;
    if (accountNo != other.accountNo)
      return false;
    if (activeDate == null) {
      if (other.activeDate != null)
        return false;
    } else if (!activeDate.equals(other.activeDate))
      return false;
    if (balance == null) {
      if (other.balance != null)
        return false;
    } else if (!balance.equals(other.balance))
      return false;
    if (contact_address1 == null) {
      if (other.contact_address1 != null)
        return false;
    } else if (!contact_address1.equals(other.contact_address1))
      return false;
    if (contact_address2 == null) {
      if (other.contact_address2 != null)
        return false;
    } else if (!contact_address2.equals(other.contact_address2))
      return false;
    if (contact_city == null) {
      if (other.contact_city != null)
        return false;
    } else if (!contact_city.equals(other.contact_city))
      return false;
    if (contact_email == null) {
      if (other.contact_email != null)
        return false;
    } else if (!contact_email.equals(other.contact_email))
      return false;
    if (contact_number == null) {
      if (other.contact_number != null)
        return false;
    } else if (!contact_number.equals(other.contact_number))
      return false;
    if (contact_state == null) {
      if (other.contact_state != null)
        return false;
    } else if (!contact_state.equals(other.contact_state))
      return false;
    if (contact_zip == null) {
      if (other.contact_zip != null)
        return false;
    } else if (!contact_zip.equals(other.contact_zip))
      return false;
    if (firstName == null) {
      if (other.firstName != null)
        return false;
    } else if (!firstName.equals(other.firstName))
      return false;
    if (inactiveDate == null) {
      if (other.inactiveDate != null)
        return false;
    } else if (!inactiveDate.equals(other.inactiveDate))
      return false;
    if (lastName == null) {
      if (other.lastName != null)
        return false;
    } else if (!lastName.equals(other.lastName))
      return false;
    if (middleName == null) {
      if (other.middleName != null)
        return false;
    } else if (!middleName.equals(other.middleName))
      return false;
    if (packageList == null) {
      if (other.packageList != null)
        return false;
    } else if (!packageList.equals(other.packageList))
      return false;
    if (serviceInstanceList == null) {
      if (other.serviceInstanceList != null)
        return false;
    } else if (!serviceInstanceList.equals(other.serviceInstanceList))
      return false;
    return true;
  }

}
