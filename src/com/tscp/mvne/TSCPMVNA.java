package com.tscp.mvne;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.mail.internet.InternetAddress;
import javax.xml.ws.WebServiceException;

import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telscape.billingserviceinterface.Payment;
import com.telscape.billingserviceinterface.PaymentHolder;
import com.telscape.billingserviceinterface.UsageHolder;
import com.tscp.mvne.billing.Account;
import com.tscp.mvne.billing.AccountStatus;
import com.tscp.mvne.billing.BillingUtil;
import com.tscp.mvne.billing.contract.ContractService;
import com.tscp.mvne.billing.contract.KenanContract;
import com.tscp.mvne.billing.contract.exception.ContractException;
import com.tscp.mvne.billing.exception.BillingException;
import com.tscp.mvne.billing.exception.ProvisionException;
import com.tscp.mvne.billing.provisioning.Component;
import com.tscp.mvne.billing.provisioning.Package;
import com.tscp.mvne.billing.provisioning.ServiceInstance;
import com.tscp.mvne.billing.provisioning.service.ProvisionService;
import com.tscp.mvne.billing.service.BillService;
import com.tscp.mvne.billing.usage.UsageDetail;
import com.tscp.mvne.billing.usage.UsageSummary;
import com.tscp.mvne.config.DEVICE;
import com.tscp.mvne.config.DOMAIN;
import com.tscp.mvne.config.NOTIFICATION;
import com.tscp.mvne.config.PROVISION;
import com.tscp.mvne.customer.Customer;
import com.tscp.mvne.customer.CustomerException;
import com.tscp.mvne.customer.DeviceException;
import com.tscp.mvne.customer.dao.CustAcctMapDAO;
import com.tscp.mvne.customer.dao.CustAddress;
import com.tscp.mvne.customer.dao.CustInfo;
import com.tscp.mvne.customer.dao.CustTopUp;
import com.tscp.mvne.device.Device;
import com.tscp.mvne.device.DeviceAssociation;
import com.tscp.mvne.device.DeviceStatus;
import com.tscp.mvne.device.service.DeviceService;
import com.tscp.mvne.exception.MVNEException;
import com.tscp.mvne.hibernate.HibernateUtil;
import com.tscp.mvne.network.NetworkInfo;
import com.tscp.mvne.network.NetworkInfoUtil;
import com.tscp.mvne.network.exception.NetworkException;
import com.tscp.mvne.network.service.NetworkService;
import com.tscp.mvne.notification.NotificationCategory;
import com.tscp.mvne.notification.NotificationSender;
import com.tscp.mvne.notification.NotificationType;
import com.tscp.mvne.notification.dao.EmailNotification;
import com.tscp.mvne.notification.dao.NotificationParameter;
import com.tscp.mvne.notification.exception.NotificationException;
import com.tscp.mvne.notification.template.Template;
import com.tscp.mvne.payment.PaymentException;
import com.tscp.mvne.payment.PaymentType;
import com.tscp.mvne.payment.dao.CreditCard;
import com.tscp.mvne.payment.dao.CustPmtMap;
import com.tscp.mvne.payment.dao.PaymentInvoice;
import com.tscp.mvne.payment.dao.PaymentRecord;
import com.tscp.mvne.payment.dao.PaymentTransaction;
import com.tscp.mvne.payment.dao.PaymentUnitResponse;
import com.tscp.mvne.payment.response.PaymentUnitMsg;
import com.tscp.mvne.refund.KenanPayment;
import com.tscp.mvne.refund.RefundException;
import com.tscp.mvne.refund.RefundService;
import com.tscp.mvne.util.logger.MethodLogger;

@WebService
public class TSCPMVNA {
	private static Logger logger = LoggerFactory.getLogger("TSCPMVNA");
	private static NetworkService networkService;
	private static BillService billService;
	private static ContractService contractService;
	private static RefundService refundService;
	private static ProvisionService provisionService;
	private static DeviceService deviceService;
	private static NotificationSender notificationSender;

	@WebMethod
	public void refundPayment(
			int accountNo,
			int transId,
			String amount,
			int trackingId,
			String refundBy,
			int refundCode,
			String notes) throws RefundException {
		MethodLogger.logMethod("refundPayment", accountNo, amount, trackingId, refundBy);
		refundService.refundPayment(accountNo, transId, trackingId, amount, refundBy, refundCode, notes);
		MethodLogger.logMethodExit("refundPayment");
	}

	@WebMethod
	public PaymentTransaction getPaymentTransaction(
			int custId,
			int transId) throws PaymentException {
		MethodLogger.logMethod("getPaymentTransaction", custId, transId);
		PaymentTransaction paymentTransaction = refundService.getPaymentTransaction(custId, transId);
		MethodLogger.logMethodReturn("getPaymentTransaction", paymentTransaction);
		return paymentTransaction;
	}

	public TSCPMVNA() {
		init();
	}

	@WebMethod
	public NetworkInfo activateService(
			Customer customer,
			NetworkInfo networkInfo) {
		MethodLogger.logMethod("activateService", customer, networkInfo);
		networkService.activateMDN(networkInfo);
		try {
			logger.info("Attempting to update device information to active");
			if (customer.getDeviceList() == null) {
				customer.retrieveDeviceList();
			}
			if (customer.getDeviceList() != null) {
				for (Device device : customer.getDeviceList()) {
					if (device.getValue().equals(networkInfo.getEsnmeiddec()) || device.getValue().equals(networkInfo.getEsnmeidhex())) {
						logger.info("Found Device Information " + device.getId() + " for Customer " + customer.getId() + ".");
						device.setStatusId(DeviceStatus.ACTIVE.getValue());
						device.setStatus(DeviceStatus.ACTIVE.getDescription());
						device.setEffectiveDate(new Date());
						device.save();
					}
				}
			}
		} catch (Exception ex) {
			logger.info("Error updating device information in activation method ");
			logger.warn(ex.getMessage());
		}
		MethodLogger.logMethodReturn("activateService", networkInfo);
		return networkInfo;
	}

	@WebMethod
	public CreditCard addCreditCard(
			Customer customer,
			CreditCard creditCard) {
		MethodLogger.logMethod("addCreditCard", customer, creditCard);
		if (creditCard.getPaymentid() != 0) {
			throw new PaymentException("addCreditCard", "PaymentID must be 0 when adding a payment");
		}
		CreditCard insertedCreditCard = customer.insertCreditCardPaymentInformation(creditCard);
		paymentUpdatedRoutine(customer);
		MethodLogger.logMethodReturn("addCreditCard", insertedCreditCard);
		return insertedCreditCard;
	}

	@WebMethod
	public List<CustAddress> addCustAddress(
			Customer customer,
			CustAddress custAddress) {
		if (customer == null) {
			throw new CustomerException("Invalid customer object");
		}
		if (custAddress == null || custAddress.getAddressId() != 0) {
			throw new CustomerException("Invalid customer address object ");
		}
		if (custAddress.getCustId() != customer.getId()) {
			throw new CustomerException("Invalid action...cannot save address for this customer");
		}
		custAddress.save();
		return customer.getCustAddressList(custAddress.getAddressId());
	}

	@WebMethod
	public Device addDeviceInfoObject(
			Customer customer,
			Device device) {
		MethodLogger.logMethod("addDeviceInfoObject", customer, device);
		if (customer == null) {
			throw new CustomerException("Customer information must be populated");
		}
		if (device == null) {
			throw new DeviceException("Device Information must be populated");
		} else {
			if (device.getId() != 0) {
				throw new DeviceException("Cannot add a Device if the ID is already established");
			}
			if (device.getAccountNo() <= 0) {
				throw new DeviceException("Account Number cannot be empty");
			}
		}
		if (customer.getId() != device.getCustId()) {
			throw new CustomerException("Cannot save a device to a different customer");
		}
		device.save();
		MethodLogger.logMethodReturn("addDeviceInfoObject", device);
		return device;
	}

	@WebMethod
	public void applyChargeCredit(
			CreditCard creditCard,
			String amount) {
		MethodLogger.logMethod("applyChargeCredit", creditCard, amount);
		refundService.applyChargeCredit(creditCard, amount);
		MethodLogger.logMethodExit("applyChargeCredit");
	}

	@WebMethod
	public int applyContract(
			KenanContract contract) {
		MethodLogger.logMethod("applyContract", contract);
		int contractId = contractService.applyContract(contract);
		logger.info("Contract " + contract.getContractType() + " applied for account " + contract.getAccount().getAccountNo() + " on MDN "
				+ contract.getServiceInstance().getExternalId());
		MethodLogger.logMethodExit("applyContract");
		return contractId;
	}

	@WebMethod
	public int applyCouponPayment(
			Account account,
			String amount,
			Date date) {
		MethodLogger.logMethod("applyCouponPayment", account, amount, date);
		int trackingId = contractService.applyCouponPayment(account, amount, date);
		MethodLogger.logMethodExit("applyCouponPayment");
		return trackingId;
	}

	private void bindServiceInstanceObject(
			Account account,
			ServiceInstance serviceInstance) {
		List<ServiceInstance> serviceInstanceList = billService.getServiceInstanceList(account);
		if (serviceInstanceList != null) {
			for (ServiceInstance si : serviceInstanceList) {
				if (si.getExternalId().equals(serviceInstance.getExternalId())) {
					serviceInstance.setExternalIdType(si.getExternalIdType());
				}
			}
		} else {
			throw new WebServiceException("Active ExternalIDs not found for Account Number " + account.getAccountNo());
		}
	}

	@WebMethod
	public Account createBillingAccount(
			Customer customer,
			Account account) {
		MethodLogger.logMethod("createBillingAccount", customer, account);
		if (customer == null || customer.getId() <= 0) {
			throw new WebServiceException("Please specify a customer prior to adding an account.");
		}
		int accountNumber = billService.createAccount(account);
		if (accountNumber <= 0) {
			throw new WebServiceException("Error when building account...No account number returned...");
		} else {
			customer.addCustAccts(account);
		}
		MethodLogger.logMethodReturn("createBillingAccount", account);
		return account;
	}

	private void createReinstallServiceInstance(
			Account account,
			ServiceInstance serviceInstance,
			Device device) {
		MethodLogger.logMethod("createReinstallServiceInstance", account, serviceInstance, device);
		try {
			account.setServiceinstancelist(billService.getServiceInstanceList(account));
		} catch (BillingException bill_ex) {
			logger.warn("Billing Exception thrown " + bill_ex.getMessage());
		}
		logger.info("adding service instance");
		billService.addServiceInstance(account, serviceInstance);
		com.tscp.mvne.billing.provisioning.Package lPackage = new com.tscp.mvne.billing.provisioning.Package();
		logger.info("adding package");
		billService.addPackage(account, serviceInstance, lPackage);
		com.tscp.mvne.billing.provisioning.Component componentid = new com.tscp.mvne.billing.provisioning.Component();
		componentid.setId(PROVISION.COMPONENT.REINSTALL);
		logger.info("adding Component");
		billService.addComponent(account, serviceInstance, lPackage, componentid);

		// Update device association for this customer
		try {
			logger.info("Adding new association");
			// logger.info("Updating Device Association");
			logger.info("attempting to retrieve subscr_no for EXTERNAL_ID " + serviceInstance.getExternalId());
			List<ServiceInstance> serviceInstanceList = billService.getServiceInstanceList(account);
			if (serviceInstanceList != null) {
				for (ServiceInstance tempServiceInstance : serviceInstanceList) {
					if (tempServiceInstance.getExternalId().equals(serviceInstance.getExternalId())) {
						logger.info("Subscriber " + tempServiceInstance.getSubscriberNumber() + " found");
						logger.info("Building Device Association Mapping");
						DeviceAssociation deviceAssociation = new DeviceAssociation();
						deviceAssociation.setDeviceId(device.getId());
						deviceAssociation.setSubscrNo(tempServiceInstance.getSubscriberNumber());
						deviceAssociation.setValue(device.getValue());
						deviceAssociation.setStatus(DeviceStatus.ACTIVE.getValue());
						logger.info("Saving device association");
						deviceAssociation.save();
						//
						if (device.getStatusId() != DeviceStatus.ACTIVE.getValue()) {
							logger.info("DeviceInfo " + device.getId() + " is not in active status...Activating");
							device.setStatusId(DeviceStatus.ACTIVE.getValue());
							device.setStatus(DeviceStatus.ACTIVE.getDescription());
							device.setEffectiveDate(new Date());
							device.save();
						}
						break;
					}
				}
			}
		} catch (Exception ex) {
			logger.info("Error updating Device Association in createReinstallServiceInstance method");
			logger.warn(ex.getMessage());
		}
		MethodLogger.logMethodExit("createReinstallServiceInstance");
	}

	@WebMethod
	public Account createServiceInstance(
			Account account,
			ServiceInstance serviceInstance) {
		MethodLogger.logMethod("createServiceInstance", account, serviceInstance);
		try {
			account.setServiceinstancelist(billService.getServiceInstanceList(account));
		} catch (BillingException bill_ex) {
			logger.warn("Billing Exception thrown " + bill_ex.getMessage());
			return account;
		}
		logger.info("adding service instance");
		billService.addServiceInstance(account, serviceInstance);
		com.tscp.mvne.billing.provisioning.Package lPackage = new com.tscp.mvne.billing.provisioning.Package();
		logger.info("adding package");
		billService.addPackage(account, serviceInstance, lPackage);
		com.tscp.mvne.billing.provisioning.Component componentid = null;
		logger.info("adding Component");
		billService.addComponent(account, serviceInstance, lPackage, componentid);

		if (account.getServiceinstancelist() == null) {
			account.setServiceinstancelist(new Vector<ServiceInstance>());
		}
		account.getServiceinstancelist().add(serviceInstance);
		if (account.getPackageList() == null) {
			account.setPackageList(new Vector<com.tscp.mvne.billing.provisioning.Package>());
		}
		if (lPackage.getComponentList() == null) {
			lPackage.setComponentList(new Vector<com.tscp.mvne.billing.provisioning.Component>());
		}
		lPackage.getComponentList().add(componentid);
		account.getPackageList().add(lPackage);

		// Update device association for this customer
		try {
			logger.info("Updating Device Association");
			Customer customer = new Customer();
			CustAcctMapDAO custAcctMapDAO = customer.getCustAcctMapDAOfromAccount(account.getAccountNo());
			if (custAcctMapDAO != null) {
				customer.setId(custAcctMapDAO.getCust_id());

				logger.info("Retrieving network information for MDN " + serviceInstance.getExternalId());
				NetworkInfo networkInfo = networkService.getNetworkInfo(null, serviceInstance.getExternalId());
				if (networkInfo == null) {
					throw new NetworkException("Cannot find Active device information for mdn " + serviceInstance.getExternalId());
				}

				logger.info("Retrieving customer " + customer.getId() + "'s device list");
				List<Device> deviceInfoList = new Vector<Device>();
				deviceInfoList = customer.retrieveDeviceList();
				if (deviceInfoList != null) {
					for (Device device : deviceInfoList) {
						if (device.getValue().equals(networkInfo.getEsnmeiddec()) || device.getValue().equals(networkInfo.getEsnmeidhex())) {
							logger.info("found device " + device.getId() + "...");
							logger.info("attempting to retrieve subscr_no for EXTERNAL_ID " + serviceInstance.getExternalId());
							List<ServiceInstance> serviceInstanceList = billService.getServiceInstanceList(account);
							if (serviceInstanceList != null) {
								for (ServiceInstance si : serviceInstanceList) {
									if (si.getExternalId().equals(serviceInstance.getExternalId())) {
										logger.info("Subscriber " + si.getSubscriberNumber() + " found");
										logger.info("Building Device Association Mapping");
										DeviceAssociation deviceAssociation = new DeviceAssociation();
										deviceAssociation.setDeviceId(device.getId());
										deviceAssociation.setSubscrNo(si.getSubscriberNumber());
										deviceAssociation.setValue(device.getValue());
										deviceAssociation.setStatus(DeviceStatus.ACTIVE.getValue());
										logger.info("Saving device association");
										deviceAssociation.save();

										if (device.getStatusId() != DeviceStatus.ACTIVE.getValue()) {
											logger.info("DeviceInfo " + device.getId() + " is not in active status...Activating");
											device.setStatusId(DeviceStatus.ACTIVE.getValue());
											device.setStatus(DeviceStatus.ACTIVE.getDescription());
											device.setEffectiveDate(new Date());
											device.save();
										}
										break;
									}
								}
							}
							break;
						}
					}
				}
			} else {
				logger.info("No Customer mapping found for Account Number " + account.getAccountNo());
			}
		} catch (Exception ex) {
			logger.info("Error updating Device Association in createServiceInstance method");
			logger.warn(ex.getMessage());
		}
		MethodLogger.logMethodReturn("createServiceInstance", account);
		return account;
	}

	/**
	 * 
	 * @param customer
	 * @param paymentId
	 * @return
	 */
	@WebMethod
	public List<CustPmtMap> deleteCreditCardPaymentMethod(
			Customer customer,
			int paymentId) {
		MethodLogger.logMethod("deleteCreditCardPaymentMethod", customer, paymentId);
		if (paymentId == 0) {
			throw new PaymentException("deleteCreditCardPaymentMethod", "PaymentID cannot be 0 when deleting a payment");
		}
		customer.deletePayment(paymentId);
		MethodLogger.logMethodExit("deleteCreditCardPaymentMethod");
		return customer.getCustpmttypes(0);
	}

	@WebMethod
	public void deleteCustAcctMapReference(
			Customer customer,
			Account account) {
		if (customer == null) {
			throw new CustomerException("Invalid customer object");
		}
		if (account == null || account.getAccountNo() <= 0) {
			throw new BillingException("Invalid Account object");
		}
		customer.deleteCustAccts(account);
	}

	@WebMethod
	public List<CustAddress> deleteCustAddress(
			Customer customer,
			CustAddress custAddress) {
		if (customer == null) {
			throw new CustomerException("Invalid customer object");
		}
		if (custAddress == null || custAddress.getAddressId() <= 0) {
			throw new CustomerException("Invalid customer address object ");
		}
		if (custAddress.getCustId() != customer.getId()) {
			throw new CustomerException("Invalid action...cannot save address for this customer");
		}

		custAddress.delete();
		return customer.getCustAddressList(custAddress.getAddressId());
	}

	@WebMethod
	public List<Device> deleteDeviceInfoObject(
			Customer customer,
			Device device) {
		MethodLogger.logMethod("deleteDeviceInfoObject", customer, device);
		if (customer == null) {
			throw new CustomerException("Customer Information must be provided");
		}
		if (device == null) {
			throw new DeviceException("Device information must be provided");
		} else if (device.getId() <= 0) {
			throw new DeviceException("Invalid Device Id");
		}
		device.delete();
		MethodLogger.logMethodExit("deleteDeviceInfoObject");
		return customer.retrieveDeviceList();
	}

	/**
	 * Disconnects the device from the Network only.
	 * 
	 * @param networkInfo
	 */
	@WebMethod
	public void disconnectFromNetwork(
			NetworkInfo networkInfo) {
		logger.info("Disconnecting from Network MDN " + networkInfo.getMdn());
		networkService.disconnectService(networkInfo);
	}

	/**
	 * Disconnects the device from the Network and Kenan then updates the device's status.
	 */

	@WebMethod
	public void disconnectService(
			ServiceInstance serviceInstance) {
		MethodLogger.logMethod("disconnectService", serviceInstance);
		logger.info("Calling Disconnect Service for External ID " + serviceInstance.getExternalId());
		Account account = new Account();
		logger.info("fetching account by TN");
		try {
			account.setAccountNo(billService.getAccountNoByTN(serviceInstance.getExternalId()));
			ServiceInstance si = provisionService.getActiveService(account.getAccountNo());
			if (si.getExternalId().equals(serviceInstance.getExternalId())) {
				serviceInstance = si;
			}
			if (account.getAccountNo() == 0) {
				throw new BillingException("Unable to get account number for External ID " + serviceInstance.getExternalId());
			}
		} catch (MVNEException mvne_ex) {
			logger.warn(mvne_ex.getMessage());
			throw mvne_ex;
		}
		logger.info("binding service instance object");
		bindServiceInstanceObject(account, serviceInstance);

		logger.info("obtaining network information for ExternalId " + serviceInstance.getExternalId());
		NetworkInfo networkinfo = getNetworkInfo(null, serviceInstance.getExternalId());
		// networkinfo.setMdn(serviceInstance.getExternalid());
		logger.info("disconnecting service from network");
		disconnectFromNetwork(networkinfo);

		logger.info("disconnecting TN from BillingSystem");
		disconnectServiceInstanceFromKenan(account, serviceInstance);
		logger.info("Done calling Disconnect Service for " + serviceInstance.getExternalId());

		logger.info("begin Device cleanup");
		// Find the device information associated with the network information
		// set that device to Released / Reactivateable
		try {
			Customer customer = new Customer();
			CustAcctMapDAO custAcctMapDAO = customer.getCustAcctMapDAOfromAccount(account.getAccountNo());
			if (custAcctMapDAO != null) {
				customer.setId(custAcctMapDAO.getCust_id());
				logger.info("Retrieving device list for customer id " + customer.getId());
				if (customer.getDeviceList() == null) {
					customer.retrieveDeviceList();
				}
				if (customer.getDeviceList() != null) {
					logger.info("Customer " + customer.getId() + " has " + customer.getDeviceList().size() + " devices.");
					for (Device device : customer.getDeviceList()) {
						if (device.getValue().equals(networkinfo.getEsnmeiddec()) || device.getValue().equals(networkinfo.getEsnmeidhex())) {
							logger.info("old device information found...updating");
							device.setStatusId(DeviceStatus.RELEASED.getValue());
							device.setStatus(DeviceStatus.RELEASED.getDescription());
							device.setEffectiveDate(new Date());
							device.save();
							updateDeviceHistory(device.getId(), device.getValue(), serviceInstance, DeviceStatus.RELEASED);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.info("Error moving device information around");
			logger.warn(ex.getMessage());
		}
		MethodLogger.logMethodExit("Error moving device information around");
	}

	/**
	 * Disconnects the service in Kenan only.
	 * 
	 * @param account
	 * @param serviceInstance
	 */
	@WebMethod
	public void disconnectServiceInstanceFromKenan(
			Account account,
			ServiceInstance serviceInstance) {
		List<KenanContract> contracts = contractService.getContracts(account, serviceInstance);
		if (contracts != null) {
			for (KenanContract contract : contracts) {
				contract.setAccount(account);
				contract.setServiceInstance(serviceInstance);
				contract.setDuration(0);
				contractService.updateContract(contract);
			}
		}
		billService.deleteServiceInstance(account, serviceInstance);
	}

	@WebMethod
	public Account getAccountInfo(
			int accountNumber) {
		MethodLogger.logMethod("getAccountInfo", accountNumber);
		Account account = billService.getAccountByAccountNo(accountNumber);
		MethodLogger.logMethodReturn("getAccountInfo", account);
		return account;
	}

	@WebMethod
	public Account getUnlinkedAccount(
			int custId) {
		return billService.getUnlinkedAccount(custId);
	}

	/**
	 * Temporary "lightweight" method to get the full state of the account.
	 * 
	 * @param custId
	 * @param accountNo
	 * @param deviceId
	 * @return
	 */
	private AccountStatus getAccountStatus(
			int custId,
			int accountNo,
			Device device,
			String externalId) throws DeviceException, NetworkException, ProvisionException {
		AccountStatus accountStatus = new AccountStatus();

		// get device status
		accountStatus.setDeviceStatus(device.getStatus().toUpperCase());
		System.out.println("DEVCE STATUS IS " + device.getStatus());

		// get network status
		NetworkInfo networkInfo = getNetworkInfo(device.getValue(), null);
		System.out.println("NETWORK STATUS IS " + networkInfo.getStatus());
		if (networkInfo != null && networkInfo.getStatus() != null) {
			if (networkInfo.getStatus().equals("A")) {
				accountStatus.setNetworkStatus("ACTIVE");
			} else if (networkInfo.getStatus().equals("C")) {
				accountStatus.setNetworkStatus("CANCEL");
			} else if (networkInfo.getStatus().equals("S")) {
				accountStatus.setNetworkStatus("SUSPEND");
			} else if (networkInfo.getStatus().equals("R")) {
				accountStatus.setNetworkStatus("RESERVE");
			}
		}

		// get billing status
		Component component = provisionService.getActiveComponent(accountNo, externalId);
		System.out.println("BILLING STATUS IS " + component.getId());
		if (component.getId() == 500000) {
			accountStatus.setBillingStatus("ACTIVE");
		} else if (component.getId() == 500001) {
			accountStatus.setBillingStatus("REINSTALL");
		} else if (component.getId() == 500002) {
			accountStatus.setBillingStatus("SUSPEND");
		}

		return accountStatus;
	}

	/**
	 * Temporary "lightweight" method to get the full state of the account.
	 * 
	 * @param custId
	 * @param accountNo
	 * @param deviceId
	 * @return
	 */
	@WebMethod
	public AccountStatus getAccountStatus(
			int custId,
			int accountNo,
			int deviceId) throws DeviceException, NetworkException, ProvisionException {
		ServiceInstance serviceInstance = provisionService.getActiveService(accountNo);
		return getAccountStatus(custId, accountNo, deviceId, serviceInstance.getExternalId());
	}

	/**
	 * Temporary "lightweight" method to get the full state of the account.
	 * 
	 * @param custId
	 * @param accountNo
	 * @param deviceId
	 * @return
	 */
	private AccountStatus getAccountStatus(
			int custId,
			int accountNo,
			int deviceId,
			String externalId) throws DeviceException, NetworkException, ProvisionException {
		Device device = deviceService.getDevice(custId, deviceId, accountNo);
		return getAccountStatus(custId, accountNo, device, externalId);
	}

	@WebMethod
	public List<KenanContract> getContracts(
			Account account,
			ServiceInstance serviceInstance) {
		MethodLogger.logMethod("getContracts", account, serviceInstance);
		List<KenanContract> contracts = contractService.getContracts(account, serviceInstance);
		MethodLogger.logMethodExit("getContracts");
		return contracts;
	}

	@WebMethod
	public CreditCard getCreditCardDetail(
			int paymentId) {
		MethodLogger.logMethod("getCreditCardDetail", paymentId);
		CreditCard creditCard = new CreditCard();
		creditCard.setPaymentid(paymentId);
		creditCard.load();
		MethodLogger.logMethodReturn("getCreditCardDetail", creditCard);
		return creditCard;
	}

	@WebMethod
	public List<CustAddress> getCustAddressList(
			Customer customer,
			int addressId) {
		if (customer == null) {
			throw new CustomerException("Customer object must be specified");
		}
		if (addressId < 0) {
			throw new CustomerException("Invalid AddressId Value");
		}
		return customer.getCustAddressList(addressId);
	}

	@WebMethod
	public CustAcctMapDAO getCustFromAccount(
			int accountno) {
		Customer customer = new Customer();
		return customer.getCustAcctMapDAOfromAccount(accountno);
	}

	@WebMethod
	public CustInfo getCustInfo(
			Customer customer) {
		if (customer == null || customer.getId() <= 0) {
			throw new CustomerException("Invalid Customer object");
		}
		return customer.getCustInfo();
	}

	@WebMethod
	public List<CustAcctMapDAO> getCustomerAccounts(
			int customerId) {
		MethodLogger.logMethod("getCustomerAccounts", customerId);
		Customer cust = new Customer();
		cust.setId(customerId);
		List<CustAcctMapDAO> custAcctList = cust.getCustaccts();
		logger.info("Mapped Accounts are :");
		for (CustAcctMapDAO custAcct : custAcctList) {
			logger.info(custAcct.toString());
		}
		MethodLogger.logMethodExit("getCustomerAccounts");
		return custAcctList;
	}

	@WebMethod
	public List<UsageDetail> getCustomerChargeHistory(
			Customer customer,
			int accountNo,
			String mdn) {
		return customer.getChargeHistory(accountNo, mdn);
	}

	@WebMethod
	public List<UsageDetail> getActivity(
			int accountNo,
			String mdn,
			Date startDate,
			Date endDate) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query q;
		if (startDate == null || endDate == null) {
			q = session.getNamedQuery("sp_fetch_charge_history");
			q.setParameter("in_account_no", accountNo);
			q.setParameter("in_external_id", mdn);
		} else {
			q = session.getNamedQuery("sp_fetch_charge_history_range");
			q.setParameter("in_account_no", accountNo);
			q.setParameter("in_external_id", mdn);
			q.setParameter("in_start_date", startDate);
			q.setParameter("in_end_date", endDate);
		}
		List<UsageDetail> usageDetailList = q.list();
		session.getTransaction().rollback();
		return usageDetailList;
	}

	@WebMethod
	public PaymentInvoice getCustomerInvoice(
			Customer customer,
			int transId) {
		if (customer == null) {
			throw new CustomerException("Invalid customer object");
		}
		return customer.getPaymentInvoice(transId);
	}

	@WebMethod
	public List<CustPmtMap> getCustPaymentList(
			int customerId,
			int paymentId) {
		MethodLogger.logMethod("getCustPaymentList", customerId, paymentId);
		Customer cust = new Customer();
		cust.setId(customerId);
		List<CustPmtMap> custPaymentList = cust.getCustpmttypes(paymentId);
		logger.info("Return object");
		for (CustPmtMap custPmtMap : custPaymentList) {
			logger.info(custPmtMap.toString());
		}
		MethodLogger.logMethodExit("getCustPaymentList");
		return custPaymentList;
	}

	@WebMethod
	public CustTopUp getCustTopUpAmount(
			Customer customer,
			Account account) {
		MethodLogger.logMethod("getCustTopUpAmount", customer, account);
		CustTopUp topUp = customer.getTopupAmount(account);
		MethodLogger.logMethodReturn("getCustTopUpAmount", topUp);
		return topUp;
	}

	@WebMethod
	public List<Device> getDeviceList(
			Customer customer) {
		MethodLogger.logMethod("getDeviceList", customer);
		if (customer == null || customer.getId() <= 0) {
			throw new CustomerException("Customer object must be provided");
		}
		MethodLogger.logMethodExit("getDeviceList");
		return customer.retrieveDeviceList();
	}

	@WebMethod
	public List<KenanPayment> getKenanPayments(
			Account account) {
		MethodLogger.logMethod("getKenanPayments", account);
		List<KenanPayment> payments = refundService.getKenanPayments(account);
		MethodLogger.logMethodExit("getKenanPayments");
		return payments;
	}

	@WebMethod
	public NetworkInfo getNetworkInfo(
			String esn,
			String mdn) throws NetworkException {
		MethodLogger.logMethod("getNetworkInfo", esn, mdn);
		NetworkInfo networkInfo = networkService.getNetworkInfo(esn, mdn);
		MethodLogger.logMethodReturn("getNetworkInfo", networkInfo);
		return networkInfo;
	}

	@WebMethod
	public List<PaymentRecord> getPaymentHistory(
			Customer customer) {
		return customer.getPaymentHistory();
	}

	@WebMethod
	public NetworkInfo getSwapNetworkInfo(
			String esn,
			String mdn) {
		MethodLogger.logMethod("getSwapNetworkInfo", esn, mdn);
		NetworkInfo networkInfo = networkService.getSwapNetworkInfo(esn, mdn);
		MethodLogger.logMethodReturn("getSwapNetworkInfo", networkInfo);
		return networkInfo;
	}

	@WebMethod
	public UsageSummary getUsageSummary(
			Customer customer,
			ServiceInstance serviceInstance) {
		MethodLogger.logMethod("getUsageSummary", serviceInstance);
		UsageSummary usage = new UsageSummary();
		try {
			List<CustAcctMapDAO> accountList = customer.getCustaccts();
			boolean validRequest = false;
			if (customer == null || serviceInstance == null) {
				logger.warn("Exception being raised due to lack of customer or serviceInstance objects.");
				throw new CustomerException("Error! Check required input parameters...");
			} else {
				if (serviceInstance.getExternalId() == null || serviceInstance.getExternalId().trim().length() <= 0) {
					throw new BillingException("Error! Check that a valid ServiceInstance object is supplied....");
				}
			}
			if (accountList != null) {
				logger.info("CustAcctMap has been found to have " + accountList.size() + " elements");
				for (CustAcctMapDAO custAcct : accountList) {
					Account account = new Account();
					account.setAccountNo(custAcct.getAccount_no());
					List<ServiceInstance> serviceInstanceList = billService.getServiceInstanceList(account);
					for (ServiceInstance si : serviceInstanceList) {
						if (si.getExternalId().equals(serviceInstance.getExternalId())) {
							logger.info("request is valid");
							validRequest = true;
						}
					}
				}
			} else {
				throw new CustomerException("Error! No accounts associated with CustID " + customer.getId());
			}
			if (!validRequest) {
				throw new CustomerException("Error! Customer " + customer.getId() + " is not associated with ServiceInstance " + serviceInstance.getExternalId());
			}
			UsageHolder usageHolder = billService.getUnbilledUsageSummary(serviceInstance);
			if (usageHolder != null) {
				if (usageHolder.getStatusMessage() != null && usageHolder.getStatusMessage().getStatus().equalsIgnoreCase("SUCCESS")) {
					usage.load(usageHolder.getUsage());
					logger.info("Usage object loaded");
					logger.info(usage.toString());
				} else if (usageHolder.getStatusMessage().getStatus().equals("")) {
					usage.setExternalid(serviceInstance.getExternalId());
					System.out.println("Empty Status...");
				} else {
					throw new BillingException("Error getting usage for " + serviceInstance.getExternalId() + ". " + usageHolder.getStatusMessage().getStatus() + "::"
							+ usageHolder.getStatusMessage().getMessage());
				}
			}
			// logger.exiting("TruConnect", "getUsageSummary");
		} catch (MVNEException ex) {
			logger.warn(ex.getMessage());
			throw ex;
		}
		MethodLogger.logMethodExit("getUsageSummary");
		return usage;
	}

	@WebMethod(exclude = true)
	public void init() {
		// logger = new TscpmvneLogger();
		logger = LoggerFactory.getLogger("TSCPMVNA");
		networkService = new NetworkService();
		billService = new BillService();
		contractService = new ContractService();
		refundService = new RefundService();
		provisionService = new ProvisionService();
		deviceService = new DeviceService();
		notificationSender = new NotificationSender();
	}

	@WebMethod
	public PaymentUnitResponse makePaymentById(
			String sessionId,
			int custId,
			int accountNo,
			int paymentId,
			String amount) {
		logger.debug("makePayment called");
		Account account = getAccountInfo(accountNo);
		Customer customer = new Customer();
		customer.setId(custId);
		return submitPaymentByPaymentId(sessionId, customer, paymentId, account, amount);
	}

	private void paymentUpdatedRoutine(
			Customer customer) {
		logger.info("Begin paymentUpdateRoutine(custId:{})", customer.getId());
		if (customer == null || customer.getId() <= 0) {
			throw new CustomerException("invalid customer object");
		}
		List<CustAcctMapDAO> accountList = customer.getCustaccts();
		Account account;
		// List<Device> devices = getDeviceList(customer);
		// Device device;
		if (accountList != null && !accountList.isEmpty()) {
			for (CustAcctMapDAO accountMap : accountList) {
				logger.info("Updating service instances for account {} to {}", accountMap.getAccount_no(), PROVISION.SERVICE.UPDATE);
				account = billService.getAccountByAccountNo(accountMap.getAccount_no());

				// temporary patch to restore all accounts that have payment updated
				// device = findDeviceInList(devices, loadedAccount.getAccountno());
				// logger.info("TMP: restoring accountNo:{} deviceId:{}",
				// loadedAccount.getAccountno(), device.getId());
				// restoreAccount(customer.getId(), loadedAccount.getAccountno(),
				// device.getId());

				for (ServiceInstance serviceInstance : account.getServiceinstancelist()) {
					logger.info("Updating threshold value for ServiceInstance {} on account {} to {}",
							new Object[] { serviceInstance.getExternalId(), account.getAccountNo(), PROVISION.SERVICE.UPDATE });
					billService.updateServiceInstanceStatus(serviceInstance, PROVISION.SERVICE.UPDATE);
				}
			}
		}
	}

	/**
	 * Reactivates the billing account in Kenan. An account is deactivated when there are no active services on Bill Run.
	 * 
	 * @param accountNumber
	 * @throws BillingException
	 */
	@WebMethod
	private void reactivateBillingAccount(
			int accountNumber) throws BillingException {
		billService.reactivateBillingAccount(accountNumber);
	}

	/**
	 * Used to reinstate the customer's Device when it is deactivated and add the appropriate component. This will assign
	 * a new MDN to the device.
	 * 
	 * @param customer
	 * @param device
	 * @return
	 */
	@WebMethod
	public NetworkInfo reinstallCustomerDevice(
			Customer customer,
			Device device) {
		MethodLogger.logMethod("reinstallCustomerDevice", customer, device);
		NetworkInfo networkInfo = new NetworkInfo();
		int accountNo = 0;
		String esn = "";
		String externalId = ""; // Old ExternalId associated with the DeviceInfo
		boolean chargeMRC = false;
		Date lastActiveDate = null;
		Date now = new Date();

		try {
			if (customer == null || customer.getId() <= 0) {
				throw new CustomerException("invalid Customer object");
			}

			if (device == null || device.getId() <= 0) {
				throw new DeviceException("Device Information must be provided");
			}

			logger.info("Retrieving device information");
			List<Device> deviceInfoList = customer.retrieveDeviceList(device.getId(), 0);
			if (deviceInfoList != null && deviceInfoList.size() > 0) {
				for (Device tempDeviceInfo : deviceInfoList) {
					accountNo = tempDeviceInfo.getAccountNo();
					esn = tempDeviceInfo.getValue();
					device = tempDeviceInfo;
				}
			} else {
				throw new DeviceException("Device Information cannot be located..does it belong to customer " + customer.getId());
			}

			logger.info("Verifying device...");
			networkInfo = getNetworkInfo(esn, null);
			if (networkInfo != null) {
				if (networkInfo.getEsnmeiddec().equals(device.getValue()) || networkInfo.getEsnmeidhex().equals(device.getValue())) {
					if (networkInfo.getStatus() != null && (networkInfo.getStatus().equals(DEVICE.ACTIVE) || networkInfo.getStatus().equals(DEVICE.SUSPENDED))) {
						throw new NetworkException("Device is currently bound to another subscriber...");
					}
				}
			}
			logger.info("Device is not already in use");

			logger.info("make sure Kenan Account isn't closed");
			Account account = getAccountInfo(accountNo);
			reactivateBillingAccount(accountNo);
			if (account != null) {
				if (account.getInactive_date() != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
					throw new BillingException("Account " + accountNo + " has been closed as of " + sdf.format(account.getInactive_date()));
				}
			} else {
				throw new BillingException("Billing account " + accountNo + " could not be located.");
			}
			logger.info("Account " + accountNo + " is active");

			logger.info("Fetch old device information");
			List<DeviceAssociation> deviceAssociationList = customer.retrieveDeviceAssociationList(device.getId());
			if (deviceAssociationList != null && deviceAssociationList.size() > 0) {
				for (DeviceAssociation deviceAssociation : deviceAssociationList) {
					logger.info("Setting the externalId and lastActiveDate " + deviceAssociation.getInactiveDate());
					externalId = deviceAssociation.getExternalId();
					lastActiveDate = deviceAssociation.getInactiveDate();
					break;
				}
				logger.info("Deactivated device's external id was " + externalId + "...using that as point of referenece");
			} else {
				logger.info("No old device associations could be found for customer " + customer.getId() + " and device id " + device.getId());
			}

			logger.info("Calculating whether to charge MRC");
			List<UsageDetail> usageDetailList = getCustomerChargeHistory(customer, accountNo, externalId);
			if (usageDetailList != null && usageDetailList.size() > 0) {
				logger.info("looking for latest access fee payment");
				for (UsageDetail usageDetail : usageDetailList) {
					if (usageDetail.getUsageType().equals("Access Fee")) {
						logger.info("Found an End Date for MRC of " + usageDetail.getEndTime());
						lastActiveDate = usageDetail.getEndTime();
						break;
					}
				}
				logger.info("Last access fee payment was made on " + lastActiveDate);
				if (lastActiveDate.getTime() > now.getTime()) {
					logger.info("MRC Charge won't be necessary");
				} else {
					logger.info("MRC will be charged");
					chargeMRC = true;
				}
			} else {
				logger.info("Charge history could not be found for customer " + customer.getId() + ", accountNo " + device.getAccountNo() + ", externalId "
						+ externalId);
			}

			logger.info("Reserving MDN");
			networkInfo = reserveMDN();
			switch (device.getValue().length()) {
				case DEVICE.ESN_DEC:
				case DEVICE.MEID_DEC:
					networkInfo.setEsnmeiddec(device.getValue());
					break;
				case DEVICE.ESN_HEX:
				case DEVICE.MEID_HEX:
					networkInfo.setEsnmeidhex(device.getValue());
					break;
				default:
					throw new DeviceException("Device Value is not of a valid length");
			}

			logger.info("Activate MDN");
			activateService(customer, networkInfo);

			ServiceInstance serviceInstance = new ServiceInstance();
			serviceInstance.setExternalId(networkInfo.getMdn());

			logger.info("Build Kenan Service Instance");
			if (!chargeMRC) {
				createReinstallServiceInstance(account, serviceInstance, device);
			} else {
				createServiceInstance(account, serviceInstance);
			}

		} catch (MVNEException mvne_ex) {
			logger.warn(mvne_ex.getMessage());
			MethodLogger.logMethodExit("reinstallCustomerDevice");
			throw mvne_ex;
		}
		MethodLogger.logMethodReturn("reinstallCustomerDevice", networkInfo);
		return networkInfo;
	}

	@WebMethod
	public NetworkInfo reserveMDN() {
		MethodLogger.logMethod("reserveMdn");
		String csa = null;
		String priceplan = null;
		List<String> soclist = null;
		NetworkInfo networkInfo = networkService.reserveMDN(csa, priceplan, soclist);
		MethodLogger.logMethodReturn("reserveMdn", networkInfo);
		return networkInfo;
	}

	@WebMethod
	public void restoreAccount(
			int custId,
			int accountNo,
			int deviceId) throws BillingException, ProvisionException, DeviceException, NetworkException {
		logger.info("Begin restoreAccount(custId:{},accountNo:{},deviceId:{})", new Object[] { custId, accountNo, deviceId });
		ServiceInstance serviceInstance = provisionService.getActiveService(accountNo);
		logger.debug("   ...restoreAccount[{}] with MDN {}", accountNo, serviceInstance.getExternalId());
		Component component = provisionService.getActiveComponent(accountNo, serviceInstance.getExternalId());
		logger.debug("   ...restoreAccount[{}] with active component {} instance {}", new Object[] { accountNo, component.getId(), component.getInstanceId() });

		Device device = deviceService.getDevice(custId, deviceId, accountNo);
		NetworkInfo deviceNetworkInfo = getNetworkInfo(device.getValue(), null);
		NetworkInfo accountNetworkInfo = getNetworkInfo(null, serviceInstance.getExternalId());
		NetworkInfoUtil.checkNetworkInfoMatch(deviceNetworkInfo, accountNetworkInfo);

		if (component.getId() == PROVISION.COMPONENT.SUSPEND) {
			Package pkg = provisionService.getActivePackage(accountNo, component.getPackageInstanceId());

			boolean chargeMRC = BillingUtil.checkChargeMRCByComponent(component);
			int componentId = chargeMRC ? PROVISION.COMPONENT.INSTALL : PROVISION.COMPONENT.REINSTALL;

			// first add active component to allow for usage tracking
			logger.info("   ...restoreAccount[{}] adding SUSPEND compoent to active package {} instance {}",
					new Object[] { accountNo, pkg.getId(), pkg.getInstanceId() });
			if (component.getActiveDate().getDayOfMonth() == new DateTime().getDayOfMonth() + 1) {
				provisionService.removeComponentNextDay(accountNo, serviceInstance.getExternalId(), pkg.getInstanceId(), component.getInstanceId());
			} else {
				provisionService.removeComponentToday(accountNo, serviceInstance.getExternalId(), pkg.getInstanceId(), component.getInstanceId());
			}
			provisionService.addSingleComponentToday(accountNo, serviceInstance.getExternalId(), pkg.getInstanceId(), componentId);
		} else {
			logger.info("   ...restoreAccount[{}] active component is not SUSPEND in Kenan, no need to update", accountNo);
		}

		// update the service threshold to allow future top-ups
		logger.info("   ...restoreAccount[{}] updating threshold to {}", accountNo, PROVISION.SERVICE.RESTORE);
		billService.updateServiceInstanceStatus(serviceInstance, PROVISION.SERVICE.RESTORE);

		// restore device to allow usage
		if (accountNetworkInfo.getStatus().equals("S")) {
			logger.info("   ...restoreAccount[{}] restoring device with Status:{} ESN:{} MDN{}", new Object[] { accountNo, accountNetworkInfo.getStatus(),
					accountNetworkInfo.getEsnmeiddec(), accountNetworkInfo.getMdn() });
			networkService.restoreService(accountNetworkInfo);
		} else {
			logger.info("   ...restoreAccount[{}] device is not suspended, no need to restore on network", accountNo);
		}

		if (device.getStatusId() != DeviceStatus.ACTIVE.getValue()) {
			logger.info("   ...restoreAccount[{}] updating device status to {}", accountNo, DeviceStatus.ACTIVE.getValue());
			device.setStatusId(DeviceStatus.ACTIVE.getValue());
			device.save();
			updateDeviceHistory(device.getId(), device.getValue(), serviceInstance, DeviceStatus.ACTIVE);
		} else {
			logger.info("   ...restoreAccount[{}] device status is already active, no need to update", accountNo);
		}
	}

	@WebMethod
	public void reverseKenanPayment(
			Account account,
			String amount,
			Date transDate,
			String trackingId) {
		MethodLogger.logMethod("reversePayment", account, amount, transDate, trackingId);
		refundService.reversePayment(account, amount, transDate, trackingId);
		MethodLogger.logMethodExit("reversePayment");
	}

	@WebMethod
	public void sendNotification(
			Customer customer,
			EmailNotification notification) {
		if (customer == null || customer.getId() <= 0)
			throw new CustomerException("Customer must be provided");
		if (notification == null)
			throw new NotificationException("Notification must be provided");
		else if (notification.getTemplate() == null)
			throw new NotificationException("Template must be specified");

		notification.setBccList(NOTIFICATION.bccList);
		notification.setFrom(NOTIFICATION.from);
		notification.setClient(NOTIFICATION.CLIENT);
		notificationSender.send(notification);
	}

	private void sendPaymentFailedNotification(
			Customer customer,
			Account account,
			PaymentTransaction paymentTransaction) {
		logger.debug("Preparing Failed Notification message");
		if (account.getFirstname() == null || account.getLastname() == null || account.getContact_email() == null) {
			logger.debug("Binding account information");
			account = billService.getAccountByAccountNo(account.getAccountNo());
		}
		if (account.getContact_email() == null || account.getContact_email().trim().isEmpty()) {
			account.setContact_email("wotgalert@telscape.net");
		}

		String customerName = account.getFirstname() + " " + account.getLastname();
		Set<NotificationParameter> notificationParametersList = new HashSet<NotificationParameter>();
		Set<InternetAddress> toList = new HashSet<InternetAddress>();
		try {
			InternetAddress to = new InternetAddress(account.getContact_email(), customerName);
			toList.add(to);
		} catch (UnsupportedEncodingException encoding_ex) {
			logger.warn(encoding_ex.getMessage());
		}
		notificationParametersList.add(new NotificationParameter("firstName", account.getFirstname()));
		notificationParametersList.add(new NotificationParameter("lastName", account.getLastname()));

		String paymentUnitMsg = paymentTransaction.getPaymentUnitMessage().toLowerCase();
		String customerMsg = null;
		for (PaymentUnitMsg msg : PaymentUnitMsg.values()) {
			if (msg.getMsg().toLowerCase().contains(paymentUnitMsg)) {
				switch (msg) {
					case INVALID_CARD:
						customerMsg = "Invalid Card";
						break;
					case INVALID_CARD_NUM:
						customerMsg = "Invalid Card Number";
						break;
					case INVALID_EXP_DATE:
						customerMsg = "Invalid Expiration Date";
						break;
					case ERR_DECLINED:
						customerMsg = "Declined";
						break;
					case ERR_CVV:
						customerMsg = "Declined, CVV code";
						break;
					case ERR_EXPIRED:
						customerMsg = "Card Expired";
						break;
					default:
						customerMsg = null;
						break;
				}
			}
		}
		notificationParametersList.add(new NotificationParameter("failureMsg", customerMsg));

		EmailNotification notification = new EmailNotification();
		notification.setCategory(NotificationCategory.WARNING);
		notification.setType(NotificationType.EMAIL);
		notification.setTemplate(Template.TOPUP_FAILURE);
		notification.setToList(toList);
		notification.setFrom(NOTIFICATION.from);
		notification.setBccList(NOTIFICATION.bccList);
		notification.setSubject("Urgent Attention Required � Web on the Go Suspended");
		notification.setParameters(notificationParametersList);
		notification.setCustId(customer.getId());
		logger.info("Sending " + notification.getTemplate() + " email");
		notification.setClient(NOTIFICATION.CLIENT);
		notificationSender.send(notification);
	}

	private void sendPaymentSuccessNotification(
			Customer customer,
			Account account,
			PaymentTransaction paymentTransaction) {
		assert customer != null && customer.getId() > 0 : "Customer invalid";
		assert account != null && account.getAccountNo() > 0 : "Account invalid";
		logger.debug("retrieving top up amount");
		CustTopUp custTopUp = customer.getTopupAmount(account);

		if (account.getFirstname() == null || account.getLastname() == null || account.getContact_email() == null) {
			logger.debug("Binding account information");
			account = billService.getAccountByAccountNo(account.getAccountNo());
		}
		assert account.getContact_email() != null : "Email is blank";

		Device device = null;
		try {
			logger.debug("binding device information");
			logger.debug("getting device information for CustomerId " + customer.getId() + " and account number " + account.getAccountNo());
			List<Device> deviceInfoList = customer.retrieveDeviceList(account.getAccountNo());
			if (deviceInfoList != null) {
				logger.debug("Customer has " + deviceInfoList.size() + " devices...binding to the first one...");
				for (Device tempDeviceInfo : deviceInfoList) {
					device = tempDeviceInfo;
					logger.debug(device.toString());
					break;
				}
			}
			if (device == null) {
				throw new NullPointerException("Device information not found");
			}
		} catch (Exception ex) {
			logger.warn("Error Binding device information...Using Account number instead...");
			logger.warn(ex.getMessage());
			device = new Device();
			device.setLabel(account.getFirstname() + "'s Account " + account.getAccountNo());
		}

		assert paymentTransaction != null : "Unable to send notification without a valid transaction for Customer " + customer.getId() + ".";

		logger.debug("Binding payment information");

		CreditCard creditCard = getCreditCardDetail(paymentTransaction.getPmtId());
		assert creditCard != null : "PaymentInformation could not be found";

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

		logger.debug("loading parameters");

		Set<NotificationParameter> notificationParameterList = new HashSet<NotificationParameter>();

		notificationParameterList.add(new NotificationParameter("firstName", account.getFirstname()));
		notificationParameterList.add(new NotificationParameter("lastName", account.getLastname()));
		// balance
		String accountBalance = account.getBalance() == null ? "0.00000" : account.getBalance();
		double balance = Double.parseDouble(accountBalance) + Double.parseDouble(paymentTransaction.getPaymentAmount());
		notificationParameterList.add(new NotificationParameter("balance", NumberFormat.getCurrencyInstance().format(balance)));

		// truconnectManagesite
		notificationParameterList.add(new NotificationParameter("manageSite", DOMAIN.urlManage));

		// billAddress1
		String billAddress1 = creditCard.getAddress1() == null ? " " : creditCard.getAddress1();
		notificationParameterList.add(new NotificationParameter("billAddress1", billAddress1));
		// billAddress2
		String billAddress2 = creditCard.getAddress2() == null ? " " : creditCard.getAddress2();
		notificationParameterList.add(new NotificationParameter("billAddress2", billAddress2));
		// billCity
		String billCity = creditCard.getCity() == null ? " " : creditCard.getCity();
		notificationParameterList.add(new NotificationParameter("billCity", billCity));
		// billState
		notificationParameterList.add(new NotificationParameter("billState", creditCard.getState()));
		// billZip
		notificationParameterList.add(new NotificationParameter("billZip", creditCard.getZip()));

		// pmtDate
		notificationParameterList.add(new NotificationParameter("pmtDate", sdf.format(paymentTransaction.getBillingUnitDate())));
		// invoiceNumber --Currently BillTrackingId
		notificationParameterList.add(new NotificationParameter("invoiceNumber", Integer.toString(paymentTransaction.getBillingTrackingId())));

		// deviceLabel
		notificationParameterList.add(new NotificationParameter("deviceLabel", device.getLabel()));

		// pmt source
		String source = "*-" + paymentTransaction.getPaymentSource();
		if (paymentTransaction.getPaymentMethod().equals("AmericanExpress")) {
			source = "-*" + paymentTransaction.getPaymentSource();
		}
		source = paymentTransaction.getPaymentMethod() + " " + source;
		notificationParameterList.add(new NotificationParameter("pmtSource", source));
		// quantity
		double quantity = 1.0;
		DecimalFormat df = new DecimalFormat("0");
		quantity = Double.parseDouble(paymentTransaction.getPaymentAmount()) / Double.parseDouble(custTopUp.getTopupAmount());
		notificationParameterList.add(new NotificationParameter("quantity", df.format(quantity)));
		// topupAmount
		notificationParameterList.add(new NotificationParameter("topupAmount", NumberFormat.getCurrencyInstance().format(
				Double.parseDouble(custTopUp.getTopupAmount()))));
		// total = quantity*topupAmount
		notificationParameterList.add(new NotificationParameter("total", NumberFormat.getCurrencyInstance().format(
				Double.parseDouble(custTopUp.getTopupAmount()) * quantity)));

		// subTotal = sum(items)
		Double subTotal = Double.parseDouble(custTopUp.getTopupAmount()) * quantity;
		notificationParameterList.add(new NotificationParameter("subTotal", NumberFormat.getCurrencyInstance().format(subTotal)));
		// taxRate = 0
		double taxRate = 0;
		notificationParameterList.add(new NotificationParameter("taxRate", taxRate == 0 ? "0.00" : Double.toString(taxRate)));

		// taxedAmount = taxRate * subTotal
		double taxedAmount = subTotal * taxRate;
		notificationParameterList.add(new NotificationParameter("taxedAmount", NumberFormat.getCurrencyInstance().format(taxedAmount)));

		// totalAmountWithTax = taxedAmount + subTotal
		double totalAmountWithTax = taxedAmount + subTotal;
		notificationParameterList.add(new NotificationParameter("totalAmountWithTax", NumberFormat.getCurrencyInstance().format(totalAmountWithTax)));

		logger.debug("Parameters loaded...Preparing email for consignment");

		EmailNotification notification = new EmailNotification();

		Set<InternetAddress> toList = new HashSet<InternetAddress>();
		try {
			InternetAddress to = new InternetAddress(account.getContact_email(), account.getFirstname() + " " + account.getLastname());
			toList.add(to);
		} catch (UnsupportedEncodingException ue_ex) {

		}
		notification.setToList(toList);
		notification.setFrom(NOTIFICATION.from);
		notification.setBccList(NOTIFICATION.bccList);
		notification.setSubject("Thank you for your Web on the Go payment ");
		notification.setParameters(notificationParameterList);

		if (billAddress2 == null || billAddress2.trim().isEmpty())
			notification.setTemplate(Template.TOPUP_SUCCESS_02);
		else
			notification.setTemplate(Template.TOPUP_SUCCESS_01);

		notification.setCategory(NotificationCategory.INFO);
		notification.setType(NotificationType.EMAIL);
		notification.setCustId(customer.getId());
		notification.setClient(NOTIFICATION.CLIENT);
		notificationSender.send(notification);

		if (notification.getId() != 0) {
			logger.info("Saving pmt invoice mapping");
			PaymentInvoice paymentInvoice = new PaymentInvoice();
			paymentInvoice.setNotificationId(notification.getId());
			paymentInvoice.setTransId(paymentTransaction.getTransId());
			paymentInvoice.save();
		}
	}

	@WebMethod
	public void testSendWelcomeNotification(
			int custId,
			int accountNo) {
		Customer customer = new Customer();
		customer.setId(custId);
		Account account = getAccountInfo(accountNo);
		sendActivationSuccessNotice(customer, account);
	}

	@WebMethod
	public void testSendPaymentFailedNotification(
			int custId,
			int accountNo) {
		Customer customer = new Customer();
		customer.setId(custId);
		Account account = getAccountInfo(accountNo);
		sendPaymentFailedNotification(customer, account, null);
	}

	@WebMethod
	public void sendActivationSuccessNotice(
			Customer customer,
			Account account) {

		if (customer == null || customer.getId() <= 0)
			throw new CustomerException("invalid customer object");

		if (account == null || account.getAccountNo() <= 0)
			throw new BillingException("account object is invalid.");

		if (account.getFirstname() == null || account.getFirstname().trim().isEmpty() || account.getLastname() == null || account.getLastname().trim().isEmpty()
				|| account.getContact_email() == null || account.getContact_email().trim().isEmpty()) {
			account = billService.getAccountByAccountNo(account.getAccountNo());
		}
		if (account.getFirstname() == null || account.getFirstname().trim().isEmpty() || account.getLastname() == null || account.getLastname().trim().isEmpty()
				|| account.getContact_email() == null || account.getContact_email().trim().isEmpty()) {
			throw new BillingException("account information is incorrect for account number " + account.getAccountNo());
		}

		Set<NotificationParameter> notificationParameterList = new HashSet<NotificationParameter>();
		notificationParameterList.add(new NotificationParameter("firstName", account.getFirstname()));
		notificationParameterList.add(new NotificationParameter("lastName", account.getLastname()));
		notificationParameterList.add(new NotificationParameter("email", account.getContact_email()));
		notificationParameterList.add(new NotificationParameter("manageSite", DOMAIN.urlManage));

		EmailNotification notification = new EmailNotification();

		Set<InternetAddress> toList = new HashSet<InternetAddress>();
		try {
			InternetAddress to = new InternetAddress(account.getContact_email(), account.getFirstname() + " " + account.getLastname());
			toList.add(to);
		} catch (UnsupportedEncodingException ue_ex) {

		}
		notification.setToList(toList);
		notification.setFrom(NOTIFICATION.from);
		notification.setBccList(NOTIFICATION.bccList);
		notification.setSubject("Your new WebOnTheGo Device");
		notification.setParameters(notificationParameterList);
		notification.setTemplate(Template.ACTIVATION_SUCCESS);
		notification.setCategory(NotificationCategory.INFO);
		notification.setType(NotificationType.EMAIL);
		notification.setCustId(customer.getId());
		notification.setClient(NOTIFICATION.CLIENT);
		notificationSender.send(notification);
	}

	@WebMethod
	public void sendRegistrationSuccessNotice(
			int custId,
			String email,
			String username) {
		Set<NotificationParameter> notificationParameterList = new HashSet<NotificationParameter>();

		notificationParameterList.add(new NotificationParameter("username", username));
		notificationParameterList.add(new NotificationParameter("email", email));
		notificationParameterList.add(new NotificationParameter("MANAGE_SITE", DOMAIN.urlManage));

		EmailNotification notification = new EmailNotification();

		Set<InternetAddress> toList = new HashSet<InternetAddress>();
		try {
			InternetAddress to = new InternetAddress(email, username);
			toList.add(to);
		} catch (UnsupportedEncodingException ue_ex) {

		}
		notification.setToList(toList);
		notification.setFrom(NOTIFICATION.from);
		notification.setBccList(NOTIFICATION.bccList);
		notification.setSubject("Welcome to Web on the Go");
		notification.setParameters(notificationParameterList);
		notification.setTemplate(Template.REGISTRATION_SUCCESS);
		notification.setCategory(NotificationCategory.INFO);
		notification.setType(NotificationType.EMAIL);
		notification.setCustId(custId);
		notification.setClient(NOTIFICATION.CLIENT);
		notificationSender.send(notification);
	}

	@WebMethod
	public CustTopUp setCustTopUpAmount(
			Customer customer,
			String topUpAmount,
			Account account) {
		MethodLogger.logMethod("setCustTopUpAmount", customer, topUpAmount, account);
		CustTopUp topUp = customer.setTopupAmount(account, topUpAmount);
		MethodLogger.logMethodReturn("setCustTopUpAmount", topUp);
		return topUp;
	}

	/**
	 * Use submitPaymentByaymentId as all credit cards should be saved in the database.
	 * 
	 * @param sessionId
	 * @param account
	 * @param creditCard
	 * @param paymentAmount
	 * @return
	 */
	@Deprecated
	@WebMethod
	public PaymentUnitResponse submitPaymentByCreditCard(
			String sessionId,
			Account account,
			CreditCard creditCard,
			String paymentAmount) {
		MethodLogger.logMethod("submitPaymentByCreditCard", sessionId, account, creditCard, paymentAmount);
		if (creditCard == null) {
			logger.warn("SessionId " + sessionId + ":: CreditCard Information must be present to submit a CreditCard Payment");
			throw new PaymentException("makeCreditCardPayment", "CreditCard Information must be present to submit a CreditCard Payment");
		}
		if (paymentAmount == null || paymentAmount.indexOf(".") == 0) {
			logger.warn("Invalid payment format. Payment format needs to be \"xxx.xx\" ");
			throw new PaymentException("makeCreditCardPayment", "Invalid payment format. Payment format needs to be \"xxx.xx\" ");
		}
		if (account == null || account.getAccountNo() <= 0) {
			logger.warn("Invalid Account Object. Account must be specified when making payments...");
			throw new BillingException("makeCreditCardPayment", "Invalid Account Object. Account must be specified when making payments...");
		}
		creditCard.validate();
		logger.info("Account " + account.getAccountNo() + " is attempting to make a " + paymentAmount + " payment against Credit Card ending in "
				+ creditCard.getCreditCardNumber().subSequence(creditCard.getCreditCardNumber().length() - 4, creditCard.getCreditCardNumber().length()));

		logger.info("Creating Transaction...");
		// Create Transaction
		PaymentTransaction pmttransaction = new PaymentTransaction();
		pmttransaction.setSessionId(sessionId);
		pmttransaction.setPmtId(creditCard.getPaymentid());
		pmttransaction.setPaymentAmount(paymentAmount);
		pmttransaction.setAccountNo(account.getAccountNo());

		pmttransaction.savePaymentTransaction();
		logger.info("Transaction " + pmttransaction.getTransId() + " has been entered and is beginning");

		// Submit to payment unit
		logger.info("submitting creditcard payment");
		PaymentUnitResponse response = creditCard.submitPayment(pmttransaction);
		if (response != null) {
			logger.info(response.toString());
		} else {
			response = new PaymentUnitResponse();
			response.setConfcode("-1");
			response.setTransid("000000");
			response.setConfdescr("No response returned from payment unit");
			response.setAuthcode("System generated error input");
		}

		// update transaction
		pmttransaction.setPaymentUnitConfirmation(response.getConfirmationString());
		pmttransaction.setPaymentUnitMessage(response.getConfdescr() + " AuthCode::" + response.getAuthcode());
		pmttransaction.setPaymentUnitDate(new Date());
		String paymentMethod = "unknown";
		String paymentSource = "";
		if (creditCard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_AMEX)) {
			paymentMethod = "AmericanExpress";
			// paymentSource = "3XXX-XXXXXX-X";
		} else if (creditCard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_VISA)) {
			paymentMethod = "Visa";
			// paymentSource = "4XXX-XXXX-XXXX-";
		} else if (creditCard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_MASTERCARD)) {
			paymentMethod = "MasterCard";
			// paymentSource = "6XXX-XXXX-XXXX-";
		} else if (creditCard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_DISCOVER)) {
			paymentMethod = "Discover";
			// paymentSource = "6XXX-XXXX-XXXX-";
		}
		paymentSource += creditCard.getCreditCardNumber().substring(creditCard.getCreditCardNumber().length() - 4, creditCard.getCreditCardNumber().length());
		pmttransaction.setPaymentMethod(paymentMethod);
		pmttransaction.setPaymentSource(paymentSource);
		pmttransaction.savePaymentTransaction();

		if (response.getConfcode().equals(PaymentUnitResponse.SUCCESSFUL_TRANSACTION)) {
			// Submit to Kenan
			logger.info("adding payment information to Kenan...");
			billService.addPayment(account, paymentAmount.replace(".", ""));

			// get the tracking ID from Kenan, it should be the last entry in the list
			// of payments
			logger.info("retrieving payment list from Kenan");
			List<PaymentHolder> paymentList = billService.getPaymentHistory(account);
			if (paymentList != null && paymentList.size() > 0) {
				try {
					Payment payment = null;
					for (PaymentHolder paymentHolder : paymentList) {
						if (payment == null) {
							payment = paymentHolder.getPayment();
						}
						if (payment.getTrackingId() <= paymentHolder.getPayment().getTrackingId()) {
							payment = paymentHolder.getPayment();
						}
					}
					logger.info("Latest Billing Tracking ID found to be " + payment.getTrackingId());
					pmttransaction.setBillingTrackingId(payment.getTrackingId());
					// PaymentHolder paymentHolder =
					// paymentList.get(paymentList.size()-1);
					// logger.info("Latest Billing Tracking ID found to be "+paymentHolder.getPayment().getTrackingId());
					// pmttransaction.setBillingTrackingId(paymentHolder.getPayment().getTrackingId());
				} catch (ArrayIndexOutOfBoundsException index_ex) {
					logger.warn(index_ex.getMessage() + "...error retrieving payment item index at [" + (paymentList.size() - 1) + "]");
					pmttransaction.setBillingTrackingId(-1);
				} catch (NullPointerException np_ex) {
					logger.warn(np_ex.getMessage() + "...payment is null.");
					pmttransaction.setBillingTrackingId(-1);
				}

			}

			// update transaction
			pmttransaction.setBillingUnitDate(new Date());
			pmttransaction.savePaymentTransaction();
			logger.info("Transaction information saved and payment completed for Account " + account.getAccountNo() + ".");
		} else {
			logger.warn("Error posting credit card payment. :: " + response.getConfdescr() + " " + response.getAuthcode());
			throw new PaymentException("makeCreditCardPayment", "Error posting credit card payment. :: " + response.getConfdescr() + " " + response.getAuthcode());
		}
		MethodLogger.logMethodExit("submitPaymentByCreditCard");
		return response;
	}

	@WebMethod
	public PaymentUnitResponse submitPaymentByPaymentId(
			String sessionId,
			Customer customer,
			int paymentId,
			Account account,
			String paymentAmount) {
		MethodLogger.logMethod("submitPaymentByPaymentId", sessionId, customer, paymentId, account, paymentAmount);
		if (customer == null || customer.getId() <= 0) {
			logger.warn("SessionId " + sessionId + "::Customer cannot be empty");
			throw new CustomerException("Customer cannot be empty");
		}
		if (paymentId == 0) {
			logger.warn("SessionId " + sessionId + ":: Payment Information must be present to submit a Payment");
			throw new PaymentException("submitPaymentByPaymentId", "Payment Information must be present to submit a Payment");
		}
		if (paymentAmount == null || paymentAmount.indexOf(".") == 0) {
			logger.warn("Invalid payment format. Payment format needs to be \"xxx.xx\" ");
			throw new PaymentException("submitPaymentByPaymentId", "Invalid payment format. Payment format needs to be \"xxx.xx\" ");
		} else {
			if (Double.parseDouble(paymentAmount) % 10 != 0) {
				throw new PaymentException("submitPaymentByPaymentId", "Invalid payment amount.  Amount must be a multiple of 10");
			}
		}
		if (account == null || account.getAccountNo() <= 0) {
			logger.warn("Invalid Account Object. Account must be specified when making payments...");
			logger.info("Attempting to grab customer's account from CustAcctMap object");
			List<CustAcctMapDAO> custAcctList = customer.getCustaccts();
			if (custAcctList == null) {
				logger.warn("No accounts found for CustomerId " + customer.getId());
				throw new BillingException("submitPaymentByPaymentId", "Invalid Account Object. Account must be specified when making payments...");
			} else if (custAcctList.size() != 1) {
				logger.warn("Too many accounts found for Customer " + customer.getId());
				throw new BillingException("submitPaymentByPaymentId", "Invalid Account Object. Account must be specified when making payments...");
			} else {
				for (CustAcctMapDAO custAcctMapDAO : custAcctList) {
					account = new Account();
					account.setAccountNo(custAcctMapDAO.getAccount_no());
				}
			}
		}
		logger.info("Customer " + customer.getId() + " and Account " + account.getAccountNo() + " is attempting to make a " + paymentAmount
				+ " payment against paymentid " + paymentId);

		boolean validTransaction = false;
		String paymentMethod = "unknown";
		String paymentSource = "";

		logger.info("Retrieving payment list");
		List<CustPmtMap> custPaymentList = customer.getCustpmttypes(0);
		for (CustPmtMap custPmt : custPaymentList) {
			if (custPmt.getPaymentid() == paymentId) {
				validTransaction = true;
				logger.info("Transaction is valid...CustPmt.getPaymentType() is " + custPmt.getPaymenttype());
				if (custPmt.getPaymenttype().equals(PaymentType.CreditCard.toString())) {
					CreditCard creditcard = getCreditCardDetail(paymentId);
					if (creditcard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_AMEX)) {
						paymentMethod = "AmericanExpress";
						// paymentSource = "3XXX-XXXXXX-X";
					} else if (creditcard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_VISA)) {
						paymentMethod = "Visa";
						// paymentSource = "4XXX-XXXX-XXXX-";
					} else if (creditcard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_MASTERCARD)) {
						paymentMethod = "MasterCard";
						// paymentSource = "6XXX-XXXX-XXXX-";
					} else if (creditcard.getCreditCardNumber().substring(0, 1).equals(CreditCard.CREDITCARD_DISCOVER)) {
						paymentMethod = "Discover";
						// paymentSource = "6XXX-XXXX-XXXX-";
					}
					paymentSource += creditcard.getCreditCardNumber().substring(creditcard.getCreditCardNumber().length() - 4, creditcard.getCreditCardNumber().length());
				}
			}
		}
		if (!validTransaction) {
			logger.warn("Customer " + customer.getId() + " is not authorized to make payments from Id " + paymentId);
			throw new CustomerException("submitPaymentByPaymentId", "Customer " + customer.getId() + " is not authorized to make payments from paymentId "
					+ paymentId);
		}

		logger.info("Creating Transaction...");
		// Create Transaction
		PaymentTransaction pmttransaction = new PaymentTransaction();
		pmttransaction.setSessionId(sessionId);
		pmttransaction.setPmtId(paymentId);
		pmttransaction.setPaymentAmount(paymentAmount);
		pmttransaction.setAccountNo(account.getAccountNo());

		pmttransaction.savePaymentTransaction();
		logger.info("Transaction " + pmttransaction.getTransId() + " has been entered and is beginning");

		// Submit to payment unit
		logger.info("submitting payment information against payment id " + paymentId);
		PaymentUnitResponse response = customer.submitPayment(pmttransaction, paymentId);
		if (response != null) {
			logger.info(response.toString());
		} else {
			response = new PaymentUnitResponse();
			response.setConfcode("-1");
			response.setConfdescr("No response returned from payment unit");
			response.setAuthcode("System generated error input");
		}

		// update transaction
		pmttransaction.setPaymentUnitConfirmation(response.getConfirmationString());
		pmttransaction.setPaymentUnitMessage(response.getConfdescr() + " AuthCode::" + response.getAuthcode());
		pmttransaction.setPaymentUnitDate(new Date());
		pmttransaction.setPaymentMethod(paymentMethod);
		pmttransaction.setPaymentSource(paymentSource);
		pmttransaction.savePaymentTransaction();

		if (response.getConfcode().equals(PaymentUnitResponse.SUCCESSFUL_TRANSACTION)) {
			// Submit to Kenan
			logger.info("adding payment information to Kenan...");
			billService.addPayment(account, paymentAmount.replace(".", ""));

			// get the tracking ID from Kenan, it should be the first entry in the
			// list of payments
			List<PaymentHolder> paymentList = billService.getPaymentHistory(account);
			if (paymentList != null && !paymentList.isEmpty()) {
				Payment payment = null;
				try {
					for (PaymentHolder paymentHolder : paymentList) {
						if (payment == null) {
							payment = paymentHolder.getPayment();
						}
						if (payment.getTrackingId() <= paymentHolder.getPayment().getTrackingId()) {
							payment = paymentHolder.getPayment();
						}
					}
					logger.info("Latest Billing Tracking ID found to be " + payment.getTrackingId());
					pmttransaction.setBillingTrackingId(payment.getTrackingId());
				} catch (ArrayIndexOutOfBoundsException index_ex) {
					logger.warn(index_ex.getMessage() + "...error retrieving payment item index at [" + (paymentList.size() - 1) + "]");
					pmttransaction.setBillingTrackingId(-1);
				} catch (NullPointerException np_ex) {
					logger.warn(np_ex.getMessage() + "...payment is null.");
					pmttransaction.setBillingTrackingId(-1);
				}
			}

			// update transaction
			logger.info("Saving Transaction with Billing System Response");
			pmttransaction.setBillingUnitDate(new Date());
			pmttransaction.savePaymentTransaction();

			// send pmt notification
			logger.info("Sending payment success notification to " + account.getContact_email());
			sendPaymentSuccessNotification(customer, account, pmttransaction);

			// get device information
			logger.info("getting device information for update");
			Device device = null;
			List<Device> deviceList = customer.retrieveDeviceList(account.getAccountNo());

			AccountStatus accountStatus = null;

			// update service instances with new cleared threshold value
			logger.info("Updating service instances for account " + account.getAccountNo());
			Account loadedAccount = billService.getAccountByAccountNo(account.getAccountNo());
			for (ServiceInstance serviceInstance : loadedAccount.getServiceinstancelist()) {
				for (Device tempDevice : deviceList) {
					logger.info("Iterating through deviceInfoList");
					List<DeviceAssociation> deviceAssociationList = customer.retrieveDeviceAssociationList(tempDevice.getId());
					if (deviceAssociationList != null && !deviceAssociationList.isEmpty()) {
						logger.info("iterating through deviceAssocaitionList");
						for (DeviceAssociation deviceAssociation : deviceAssociationList) {
							if (deviceAssociation.getAccountNo() == account.getAccountNo() && deviceAssociation.getInactiveDate() == null
									&& deviceAssociation.getExternalId().equals(serviceInstance.getExternalId())) {
								logger.info("Device association found...setting device object to device id " + tempDevice.getId());
								device = tempDevice;
								accountStatus = getAccountStatus(customer.getId(), account.getAccountNo(), device, serviceInstance.getExternalId());
								break;
							}
						}
					}
					if (device != null) {
						break;
					}
				}
				// restoreSubscriber(serviceInstance, device);
				if (device != null && accountStatus != null) {
					// if (!accountStatus.getBillingStatus().equals("SUSPEND")) {
					// throw new BillingException("Account " +
					// account.getAccountNo() +
					// " does not have SUSPEND component in Kenan");
					// } else if (!accountStatus.getNetworkStatus().equals("SUSPEND")) {
					// throw new NetworkException("Device is already active on network");
					// }
					if (accountStatus.getBillingStatus().equals("SUSPEND") || accountStatus.getNetworkStatus().equals("SUSPEND")) {
						restoreAccount(customer.getId(), account.getAccountNo(), device.getId());
					}
				} else {
					throw new DeviceException("No device found to restore for Cust " + customer.getId() + " on account " + account.getAccountNo());
				}
				device = null;
			}
			logger.info("Transaction information saved and payment completed for Account " + account.getAccountNo() + ".");

		} else {
			logger.warn("Error posting credit card payment. :: " + response.getConfdescr() + " " + response.getAuthcode());

			if (pmttransaction.getSessionId().contains("AUTO") || pmttransaction.getSessionId().contains("auto")) {

				// send payment failed notification
				logger.info("Sending Payment Failed notification to " + account.getContact_email());
				sendPaymentFailedNotification(customer, account, pmttransaction);

				// get device information
				logger.info("getting device information for update");
				Device device = null;
				List<Device> deviceList = customer.retrieveDeviceList(account.getAccountNo());

				AccountStatus accountStatus = null;

				// suspend services
				logger.info("Loading account information from Billing System...");
				Account loadedAccount = billService.getAccountByAccountNo(account.getAccountNo());
				logger.info("Updating service instances for account " + account.getAccountNo());
				for (ServiceInstance serviceInstance : loadedAccount.getServiceinstancelist()) {
					logger
							.info("Updating threshold value and Network status for ServiceInstance " + serviceInstance.getExternalId() + " to " + PROVISION.SERVICE.HOTLINE);
					if (deviceList != null && !deviceList.isEmpty()) {
						for (Device tempDevice : deviceList) {
							logger.info("Iterating through deviceInfoList");
							List<DeviceAssociation> deviceAssociationList = customer.retrieveDeviceAssociationList(tempDevice.getId());
							if (deviceAssociationList != null && deviceAssociationList.size() > 0) {
								logger.info("iterating through deviceAssocaitionList");
								for (DeviceAssociation deviceAssociation : deviceAssociationList) {
									if (deviceAssociation.getAccountNo() == account.getAccountNo() && deviceAssociation.getInactiveDate() == null
											&& deviceAssociation.getExternalId().equals(serviceInstance.getExternalId())) {
										logger.info("Device association found...setting device object to device id " + tempDevice.getId());
										device = tempDevice;
										accountStatus = getAccountStatus(customer.getId(), account.getAccountNo(), device, serviceInstance.getExternalId());
										break;
									}
								}
							}
							if (device != null) {
								break;
							}
						}
					}

					// suspendSubscriber(serviceInstance, device);
					if (device != null && accountStatus != null) {
						// if (accountStatus.getBillingStatus().equals("SUSPEND")) {
						// throw new BillingException("Account " +
						// account.getAccountNo() +
						// " already has SUSPEND component");
						// } else if (accountStatus.getNetworkStatus().equals("SUSPEND")) {
						// throw new
						// NetworkException("Device is already suspended on network");
						// }
						if (accountStatus.getBillingStatus().equals("ACTIVE") || accountStatus.getBillingStatus().equals("REINSTALL")
								|| accountStatus.getNetworkStatus().equals("ACTIVE")) {
							suspendAccount(customer.getId(), account.getAccountNo(), device.getId());
						}
					} else {
						throw new DeviceException("No device found to suspend for Cust " + customer.getId() + " on account " + account.getAccountNo());
					}
					device = null;
				}
			} else {
				logger.info("No active services on this account...No need to send failed payment notification");
			}
			throw new PaymentException("submitPaymentByPaymentId", "Error posting payment. :: " + response.getConfdescr() + " " + response.getAuthcode());
		}
		MethodLogger.logMethodExit("submitPaymentByPaymentId");
		return response;

	}

	@WebMethod
	public void suspendAccount(
			int custId,
			int accountNo,
			int deviceId) throws BillingException, ProvisionException, DeviceException, NetworkException {
		logger.info("Begin suspendAccount(custId:{},accountNo:{},deviceId:{})", new Object[] { custId, accountNo, deviceId });
		ServiceInstance serviceInstance = provisionService.getActiveService(accountNo);
		logger.debug("   ...suspendAccount[{}] with MDN {}", accountNo, serviceInstance.getExternalId());
		Component component = provisionService.getActiveComponent(accountNo, serviceInstance.getExternalId());
		logger.debug("   ...suspendAccount[{}] with active component {} instance {}", new Object[] { accountNo, component.getId(), component.getInstanceId() });

		// check if the device matches the external ID in user's kenan account
		Device device = deviceService.getDevice(custId, deviceId, accountNo);
		NetworkInfo deviceNetworkInfo = getNetworkInfo(device.getValue(), null);
		NetworkInfo accountNetworkInfo = getNetworkInfo(null, serviceInstance.getExternalId());
		NetworkInfoUtil.checkNetworkInfoMatch(deviceNetworkInfo, accountNetworkInfo);

		// first suspend the network to prevent further usage
		if (accountNetworkInfo.getStatus().equals("A")) {
			logger.info("   ...suspendAccount[{}] suspending device with Status:{} ESN:{} MDN{}", new Object[] { accountNo, accountNetworkInfo.getStatus(),
					accountNetworkInfo.getEsnmeiddec(), accountNetworkInfo.getMdn() });
			networkService.suspendService(accountNetworkInfo);
		} else {
			logger.info("   ...suspendAccount[{}] device is not active, no need to supsned on network", accountNo);
		}

		// next remove component and add suspend component to prevent future MRCs
		if (component.getId() != PROVISION.COMPONENT.SUSPEND) {
			Package pkg = provisionService.getActivePackage(accountNo, component.getPackageInstanceId());
			logger.info("   ...suspendAccount[{}] adding SUSPEND compoent to active package {} instance {}",
					new Object[] { accountNo, pkg.getId(), pkg.getInstanceId() });
			provisionService.removeComponentNextDay(accountNo, serviceInstance.getExternalId(), pkg.getInstanceId(), component.getInstanceId());
			provisionService.addSingleComponentNextDay(accountNo, serviceInstance.getExternalId(), pkg.getInstanceId(), PROVISION.COMPONENT.SUSPEND);
		} else {
			logger.info("   ...suspendAccount[{}] active component is SUSPEND in Kenan, no need to update", accountNo);
		}

		// update device status on front end
		if (device.getStatusId() != DeviceStatus.SUSPENDED.getValue()) {
			logger.info("   ...suspendAccount[{}] updating device status to {}", accountNo, DeviceStatus.SUSPENDED.getValue());
			device.setStatusId(DeviceStatus.SUSPENDED.getValue());
			device.save();
			updateDeviceHistory(device.getId(), device.getValue(), serviceInstance, DeviceStatus.SUSPENDED);
		} else {
			logger.info("   ...suspendAccount[{}] device status is already inactive, no need to update", accountNo);
		}

		// finally update the service threshold to prevent future top-ups
		logger.info("   ...suspendAccount[{}] updating threshold to {}", accountNo, PROVISION.SERVICE.HOTLINE);
		billService.updateServiceInstanceStatus(serviceInstance, PROVISION.SERVICE.HOTLINE);
	}

	protected void updateDeviceHistory(
			int deviceId,
			String value,
			ServiceInstance serviceInstance,
			DeviceStatus status) {
		logger.info("updating device history for device {}:{} subscriber {} with status {}", new Object[] { deviceId, value, serviceInstance.getSubscriberNumber(),
				status.getDescription() });
		DeviceAssociation deviceAssociation = new DeviceAssociation();
		deviceAssociation.setDeviceId(deviceId);
		deviceAssociation.setValue(value);
		deviceAssociation.setSubscrNo(serviceInstance.getSubscriberNumber());
		deviceAssociation.setStatus(status.getValue());
		deviceAssociation.save();
		logger.info("finished updating device history");
	}

	@WebMethod
	public NetworkInfo swapDevice(
			Customer customer,
			NetworkInfo oldNetworkInfo,
			Device newDevice) {
		MethodLogger.logMethod("swapDevice", customer, oldNetworkInfo, newDevice);
		if (customer == null || customer.getId() <= 0) {
			throw new CustomerException("Customer object must be provided");
		}
		if (oldNetworkInfo == null || oldNetworkInfo.getMdn() == null || oldNetworkInfo.getMdn().trim().isEmpty()) {
			throw new NetworkException("Existing Network Information must be provided");
		}
		if (newDevice == null || newDevice.getValue() == null || newDevice.getValue().trim().isEmpty()) {
			throw new DeviceException("Device Information must be specified");
		} else if (newDevice.getId() <= 0) {
			throw new DeviceException("Device Id must be specified");
		}
		NetworkInfo newNetworkInfo = null;
		try {
			// String oldEsn = "";
			String oldMdn = oldNetworkInfo.getMdn();
			oldNetworkInfo = getNetworkInfo(null, oldMdn);
			newNetworkInfo = getNetworkInfo(newDevice.getValue().trim(), null);
			// newNetworkInfo = getSwapNetworkInfo

			if (newNetworkInfo != null) {
				if (newNetworkInfo.getEsnmeiddec().trim().equals(newDevice.getValue().trim())
						|| newNetworkInfo.getEsnmeidhex().trim().equals(newDevice.getValue().trim())) {
					if (newNetworkInfo.getStatus() != null) {
						if (newNetworkInfo.getStatus().equals(DEVICE.ACTIVE) || newNetworkInfo.getStatus().equals(DEVICE.SUSPENDED)
								|| newNetworkInfo.getStatus().equals(DEVICE.HOTLINED)) {
							throw new NetworkException("Device is currently assigned");
						}
						if (newNetworkInfo.getStatus().equals(DEVICE.RESERVED)) {
							throw new NetworkException("Device is currently in reserve");
						}
					}
				}
			} else {
				newNetworkInfo = new NetworkInfo();
				switch (newDevice.getValue().trim().length()) {
					case DEVICE.ESN_HEX:
					case DEVICE.MEID_HEX:
						newNetworkInfo.setEsnmeidhex(newDevice.getValue().trim());
						break;
					case DEVICE.ESN_DEC:
					case DEVICE.MEID_DEC:
						newNetworkInfo.setEsnmeiddec(newDevice.getValue().trim());
						break;
					default:
						throw new NetworkException("Invalid New Device length");
				}
				// newNetworkInfo = getNetworkInfo(newDevice.getDeviceValue(), null);
			}

			try {
				List<ServiceInstance> services = provisionService.getActiveServices(newDevice.getAccountNo());
				ServiceInstance serviceInstance = null;
				for (ServiceInstance si : services) {
					if (si.getExternalId().equals(oldNetworkInfo.getMdn())) {
						serviceInstance = si;
					}
				}

				// Send the swap request to the network
				logger.info("Sending swap request for MDN " + oldNetworkInfo.getMdn() + " to DEVICE " + newDevice.getValue());
				networkService.swapESN(oldNetworkInfo, newNetworkInfo);

				// Save device
				logger.info("Saving new device information");
				newDevice.save();

				if (serviceInstance != null) {
					updateDeviceHistory(newDevice.getId(), newDevice.getValue(), serviceInstance, DeviceStatus.ACTIVE);
				}

			} catch (NetworkException network_ex) {
				throw network_ex;
			} catch (Exception ex) {
				logger.warn(ex.getMessage());
			}
		} catch (MVNEException ex) {
			logger.warn(ex.getMessage());
			throw ex;
		}
		MethodLogger.logMethodReturn("swapDevice", newNetworkInfo);
		return newNetworkInfo;
	}

	@WebMethod
	public void updateAccountEmailAddress(
			Account account) {
		MethodLogger.logMethod("updateAccountEmailAddress", account);
		if (account == null) {
			logger.info("updatingAccountEmailAddress(account) account object is null");
			throw new BillingException("Account object cannot be null.");
		}
		logger.info("Updating EmailAddress for Account " + account.getAccountNo() + " to email address " + account.getContact_email());
		billService.updateAccountEmailAddress(account);
		MethodLogger.logMethodExit("updateAccountEmailAddress");
	}

	@WebMethod
	public void updateContract(
			KenanContract contract) {
		MethodLogger.logMethod("updateContract", contract);
		contractService.updateContract(contract);
		logger.info("Contract " + contract.getContractType() + " updated for account " + contract.getAccount().getAccountNo() + " on MDN "
				+ contract.getServiceInstance().getExternalId());
		MethodLogger.logMethodExit("updateContract");
	}

	/**
	 * 
	 * @param customer
	 * @param creditCard
	 * @return
	 */
	@WebMethod
	public List<CustPmtMap> updateCreditCardPaymentMethod(
			Customer customer,
			CreditCard creditCard) {
		MethodLogger.logMethod("updateCreditCardPaymentMethod", customer, creditCard);
		if (creditCard.getPaymentid() == 0) {
			throw new PaymentException("addCreditCard", "PaymentID cannot be 0 when updating a payment");
		}
		customer.updateCreditCardPaymentInformation(creditCard);
		logger.info("Updating service instance information");
		try {
			paymentUpdatedRoutine(customer);
		} catch (Exception ex) {
			logger.info("Error updating Service Instance information for customer " + customer.getId());
			logger.warn(ex.getMessage());
		}
		MethodLogger.logMethodExit("updateCreditCardPaymentMethod");
		return customer.getCustpmttypes(creditCard.getPaymentid());
	}

	@WebMethod
	public List<CustAddress> updateCustAddress(
			Customer customer,
			CustAddress custAddress) {
		if (customer == null) {
			throw new CustomerException("Invalid customer object");
		}
		if (custAddress == null || custAddress.getAddressId() <= 0) {
			throw new CustomerException("Invalid customer address object ");
		}
		if (custAddress.getCustId() != customer.getId()) {
			throw new CustomerException("Invalid action...cannot save address for this customer");
		}
		custAddress.save();
		return customer.getCustAddressList(custAddress.getAddressId());
	}

	@WebMethod
	public List<CustPmtMap> updateCustPaymentMap(
			CustPmtMap custPmtMap) {
		if (custPmtMap == null) {
			throw new PaymentException("CustPmtMap must be specified");
		}
		if (custPmtMap.getCustid() <= 0) {
			throw new CustomerException("Please specify customer id...");
		}
		if (custPmtMap.getPaymentid() <= 0) {
			throw new PaymentException("Please specify a payment id...Blanket update is not supported...");
		}
		Customer customer = new Customer();
		customer.setId(custPmtMap.getCustid());
		List<CustPmtMap> paymentTypes = customer.getCustpmttypes(0);
		boolean validTransaction = false;
		for (CustPmtMap tempCustPmtMap : paymentTypes) {
			if (tempCustPmtMap.getPaymentid() == custPmtMap.getPaymentid()) {
				validTransaction = true;
				break;
			}
		}
		if (!validTransaction) {
			throw new CustomerException("Customer is not authorized to make requested change");
		}
		custPmtMap.update();
		return customer.getCustpmttypes(0);
	}

	@WebMethod
	public void updateDeviceInfoObject(
			Customer customer,
			Device device) {
		MethodLogger.logMethod("updateDeviceInfoObject", customer, device);
		if (customer == null) {
			throw new CustomerException("Customer information must be populated");
		}
		if (device == null) {
			throw new DeviceException("Device Information must be populated");
		} else {
			if (device.getId() == 0) {
				throw new DeviceException("Cannot update a Device if the ID is not established");
			}
		}
		if (customer.getId() != device.getCustId()) {
			throw new CustomerException("Cannot save a device to a different customer");
		}
		device.save();
		MethodLogger.logMethodExit("updateDeviceInfoObject");
	}
}