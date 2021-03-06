package com.tscp.mvne.customer;

import java.util.List;
import java.util.Vector;

import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import com.tscp.mvne.billing.Account;
import com.tscp.mvne.billing.usage.UsageDetail;
import com.tscp.mvne.customer.dao.CustAcctMapDAO;
import com.tscp.mvne.customer.dao.CustAddress;
import com.tscp.mvne.customer.dao.CustInfo;
import com.tscp.mvne.customer.dao.CustTopUp;
import com.tscp.mvne.device.Device;
import com.tscp.mvne.device.DeviceAssociation;
import com.tscp.mvne.hibernate.GeneralSPResponse;
import com.tscp.mvne.hibernate.HibernateUtil;
import com.tscp.mvne.payment.PaymentException;
import com.tscp.mvne.payment.PaymentInformation;
import com.tscp.mvne.payment.PaymentType;
import com.tscp.mvne.payment.dao.CreditCard;
import com.tscp.mvne.payment.dao.CustPmtMap;
import com.tscp.mvne.payment.dao.PaymentInvoice;
import com.tscp.mvne.payment.dao.PaymentRecord;
import com.tscp.mvne.payment.dao.PaymentTransaction;
import com.tscp.mvne.payment.dao.PaymentUnitResponse;

@SuppressWarnings("unchecked")
public class Customer {
	private int id;
	private List<CustAcctMapDAO> custaccts;
	private List<CustPmtMap> custpmttypes;
	private List<PaymentInformation> paymentinformation;
	private List<Device> deviceList;

	public Customer() {
		paymentinformation = new Vector<PaymentInformation>();
	}

	public Customer(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(
			int id) {
		this.id = id;
	}

	public void addCustAccts(
			Account account) throws CustomerException {
		if (id <= 0) {
			throw new CustomerException("addCustAccts", "Please specify a customer to add an account mapping.");
		}
		if (account == null || account.getAccountNo() <= 0) {
			throw new CustomerException("addCustAccts", "Please specify an account to add to customer " + id);
		}
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();
		CustAcctMapDAO custacctmap = new CustAcctMapDAO();
		custacctmap.setCust_id(id);
		custacctmap.setAccount_no(account.getAccountNo());
		Query q = session.getNamedQuery("ins_cust_acct_map");
		q.setParameter("cust_id", custacctmap.getCust_id());
		q.setParameter("account_no", custacctmap.getAccount_no());
		List<GeneralSPResponse> spresponse = q.list();

		if (spresponse != null) {
			for (GeneralSPResponse response : spresponse) {
				System.out.println("STATUS :: " + response.getStatus() + " :: MVNEMSGCODE :: " + response.getCode() + " :: MVNEMSG :: " + response.getMsg());
				if (!response.getStatus().equals("Y")) {
					throw new CustomerException("addCustAccts", "Error adding Customer Acct Map:: " + response.getCode() + "::" + response.getMsg());
				}
			}
		} else {
			throw new CustomerException("addCustAccts", "No response returned from the db when calling ins_cust_acct_map");
		}

		tx.commit();
	}

	public void deleteCustAccts(
			Account account) throws CustomerException {
		if (id <= 0) {
			throw new CustomerException("addCustAccts", "Please specify a customer to add an account mapping.");
		}
		if (account == null || account.getAccountNo() <= 0) {
			throw new CustomerException("addCustAccts", "Please specify an account to add to customer " + id);
		}

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		CustAcctMapDAO custacctmap = new CustAcctMapDAO();
		custacctmap.setCust_id(id);
		custacctmap.setAccount_no(account.getAccountNo());
		Query q = session.getNamedQuery("del_cust_acct_map");
		q.setParameter("cust_id", custacctmap.getCust_id());
		q.setParameter("account_no", custacctmap.getAccount_no());
		List<GeneralSPResponse> spresponse = q.list();

		if (spresponse != null) {
			for (GeneralSPResponse response : spresponse) {
				System.out.println("STATUS :: " + response.getStatus() + " :: MVNEMSGCODE :: " + response.getCode() + " :: MVNEMSG :: " + response.getMsg());
				if (!response.getStatus().equals("Y")) {
					throw new CustomerException("addCustAccts", "Error deleting Customer Acct Map:: " + response.getCode() + "::" + response.getMsg());
				}
			}
		} else {
			throw new CustomerException("addCustAccts", "No response returned from the db when calling ins_cust_acct_map");
		}

		session.getTransaction().commit();
	}

	public void deletePayment(
			int paymentId) throws CustomerException {
		if (id == 0) {
			throw new CustomerException("deletePayment", "Invalid Customer Object. ID must be set.");
		}
		List<CustPmtMap> custPmtMapList = getCustpmttypes(0);
		boolean isValidTransaction = false;
		for (CustPmtMap cpm : custPmtMapList) {
			if (cpm.getPaymentid() == paymentId) {
				isValidTransaction = true;
				if (cpm.getPaymenttype().equals(PaymentType.CreditCard.toString())) {
					CreditCard creditcard = new CreditCard();
					creditcard.setPaymentid(paymentId);
					creditcard.deletePaymentOption();
				}
				break;
			}
		}
		if (!isValidTransaction) {
			throw new CustomerException("deletePayment", "Invalid Request. Payment ID " + paymentId + " does not belong to cust id " + id);
		}
	}

	public List<UsageDetail> getChargeHistory(
			int accountNo,
			String mdn) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query q = session.getNamedQuery("sp_fetch_charge_history");
		q.setParameter("in_account_no", accountNo);
		q.setParameter("in_external_id", mdn);

		List<UsageDetail> usageDetailList = q.list();

		session.getTransaction().rollback();
		return usageDetailList;
	}

	public List<UsageDetail> getChargeHistory(
			int accountNo,
			String mdn,
			int dayRange) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query q = session.getNamedQuery("sp_fetch_charge_history_range");
		q.setParameter("in_account_no", accountNo);
		q.setParameter("in_external_id", mdn);
		q.setParameter("in_day_range", dayRange);

		List<UsageDetail> usageDetailList = q.list();

		session.getTransaction().rollback();
		return usageDetailList;
	}

	public CustAcctMapDAO getCustAcctMapDAOfromAccount(
			int accountno) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query q = session.getNamedQuery("fetch_cust_from_acct");
		q.setParameter("in_account_no", accountno);
		List<CustAcctMapDAO> custAcctMapList = q.list();
		CustAcctMapDAO retValue = new CustAcctMapDAO();
		for (CustAcctMapDAO custAcctMap : custAcctMapList) {
			retValue = custAcctMap;
		}

		session.getTransaction().commit();
		return retValue;
	}

	public List<CustAcctMapDAO> getCustaccts() {
		if (custaccts == null) {
			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			Query q = session.getNamedQuery("fetch_cust_acct_map");
			q.setParameter("in_cust_id", id);
			custaccts = q.list();
			session.getTransaction().commit();
		}
		return custaccts;
	}

	public List<CustAddress> getCustAddressList(
			int addressId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		session.beginTransaction();

		Query q = session.getNamedQuery("fetch_cust_address");
		q.setParameter("in_cust_id", id);
		q.setParameter("in_address_id", addressId);

		List<CustAddress> custAddressList = q.list();

		session.getTransaction().rollback();
		return custAddressList;
	}

	public CustInfo getCustInfo() {
		CustInfo custInfo = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query q = session.getNamedQuery("fetch_cust_info");
		q.setParameter("in_cust_id", id);
		List<CustInfo> custInfoList = q.list();
		if (custInfoList != null && custInfoList.size() > 0) {
			custInfo = custInfoList.get(0);
		}
		session.getTransaction().commit();
		return custInfo;
	}

	public List<CustPmtMap> getCustpmttypes(
			int pmt_id) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query q = session.getNamedQuery("fetch_cust_pmt_map");
		q.setParameter("in_cust_id", id);
		q.setParameter("in_pmt_id", pmt_id);
		custpmttypes = q.list();
		session.getTransaction().commit();
		return custpmttypes;
	}

	public List<Device> getDeviceList() {
		return deviceList;
	}

	public List<PaymentRecord> getPaymentHistory() throws CustomerException {
		if (id <= 0) {
			throw new CustomerException("Invalid Customer Id " + id);
		}
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();
		List<PaymentRecord> paymentRecordList = null;

		try {
			session.setCacheMode(CacheMode.IGNORE);
			session.evict(paymentRecordList);
			Query q = session.getNamedQuery("fetch_cust_pmt_trans");
			q.setReadOnly(true);
			q.setCacheable(false);
			q.setParameter("in_cust_id", id);
			paymentRecordList = q.list();
			tx.commit();
		} catch (HibernateException he) {
			tx.rollback();
			throw new CustomerException("getPaymentHistory", he.getMessage());
		} finally {
			HibernateUtil.closeSession(session);
		}
		return paymentRecordList;
	}

	public List<PaymentInformation> getPaymentinformation() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query q = session.getNamedQuery("fetch_cust_pmt_map");
		q.setParameter("in_cust_id", id);
		q.setParameter("in_pmt_id", 0);
		custpmttypes = q.list();
		for (CustPmtMap custpmt : custpmttypes) {
			if (custpmt.getPaymentalias().equals(PaymentType.CreditCard.toString())) {
				q = session.getNamedQuery("fetch_pmt_cc_info");
				q.setParameter("in_pmt_id", custpmt.getPaymentid());
				List<CreditCard> creditcard = q.list();
				if (creditcard != null) {
					paymentinformation.add(creditcard.get(0));
				}
			}
		}
		session.getTransaction().rollback();
		return paymentinformation;
	}

	public PaymentInvoice getPaymentInvoice(
			int transId) throws CustomerException {
		if (id <= 0) {
			throw new CustomerException("Invalid Customer...Id cannot be <= 0");
		}
		if (transId == 0) {
			throw new PaymentException("Please specify a transaction to look up an invoice against");
		}
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		PaymentInvoice paymentInvoice = new PaymentInvoice();

		Query q = session.getNamedQuery("fetch_pmt_invoice");
		q.setParameter("in_cust_id", id);
		q.setParameter("in_trans_id", transId);
		List<PaymentInvoice> paymentInvoiceList = q.list();
		if (paymentInvoiceList != null && paymentInvoiceList.size() > 0) {
			for (PaymentInvoice tempPaymentInvoice : paymentInvoiceList) {
				paymentInvoice = tempPaymentInvoice;
			}
		}
		session.getTransaction().commit();
		// session.close();
		return paymentInvoice;
	}

	public CustTopUp getTopupAmount(
			Account account) throws CustomerException {
		if (id == 0) {
			throw new CustomerException("getTopupAmount", "Customer.id must be set...");
		}
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		String query = "fetch_cust_topup_amt";

		Query q = session.getNamedQuery(query);
		q.setParameter("in_cust_id", id);
		q.setParameter("in_account_no", account.getAccountNo());
		CustTopUp topupAmount = new CustTopUp();
		List<CustTopUp> topupAmountList = q.list();
		for (CustTopUp custTopUp : topupAmountList) {
			topupAmount = custTopUp;
		}
		session.getTransaction().commit();
		return topupAmount;
	}

	public CreditCard insertCreditCardPaymentInformation(
			CreditCard creditcard) throws PaymentException {
		if (id <= 0) {
			throw new CustomerException("insertCreditCardPaymentInformation", "Please specify a Customer Id");
		}
		if (creditcard == null) {
			throw new PaymentException("insertCreditCardPaymentInformation", "Please specify payment information to save.");
		}
		if (creditcard.validate()) {
			creditcard.savePaymentOption();
		}
		if (creditcard.getPaymentid() <= 0) {
			throw new PaymentException("insertCreditCardPaymentInformation", "Error saving payment information.");
		} else {
			if (creditcard.getAlias() == null || creditcard.getAlias().trim().length() == 0) {
				int myFirstCardNumber = Integer.parseInt(creditcard.getCreditCardNumber().substring(0, 1));
				String myAlias = " "
						+ creditcard.getCreditCardNumber().substring(creditcard.getCreditCardNumber().length() - 4, creditcard.getCreditCardNumber().length());
				switch (myFirstCardNumber) {
					case 3:
						myAlias = "AMEX" + myAlias;
						// throw new
						// PaymentException("insertCreditCardPaymentInformation","American Express cards are not accepted at this time. Please try another card");
						break;
					case 4:
						myAlias = "VISA" + myAlias;
						break;
					case 5:
						myAlias = "MasterCard" + myAlias;
						break;
					case 6:
						myAlias = "Discover" + myAlias;
						// throw new
						// PaymentException("insertCreditCardPaymentInformation","Discover cards are not accepted at this time. Please try another card");
						break;
				}
				creditcard.setAlias(myAlias);
			}
			saveCustPmtMap(creditcard);
		}
		return creditcard;
	}

	public List<DeviceAssociation> retrieveDeviceAssociationList(
			int inDeviceId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query q = session.getNamedQuery("fetch_device_assoc_map");
		q.setParameter("in_cust_id", id);
		q.setParameter("in_device_id", inDeviceId);
		List<DeviceAssociation> deviceAssociationList = q.list();

		session.getTransaction().rollback();
		return deviceAssociationList;
	}

	public List<Device> retrieveDeviceList() {
		return retrieveDeviceList(0, 0);
	}

	public List<Device> retrieveDeviceList(
			int accountNo) {
		return retrieveDeviceList(0, accountNo);
	}

	public List<Device> retrieveDeviceList(
			int deviceId,
			int accountNo) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query q = session.getNamedQuery("fetch_device_info");
		q.setParameter("in_cust_id", id);
		q.setParameter("in_device_id", deviceId);
		q.setParameter("in_account_no", accountNo);
		List<Device> deviceInfoList = q.list();
		setDeviceList(deviceInfoList);
		session.getTransaction().rollback();
		return getDeviceList();
	}

	private void saveCustPmtMap(
			PaymentInformation paymentinformation) throws PaymentException {

		CustPmtMap custpmtmap = new CustPmtMap();
		custpmtmap.setCustid(id);
		custpmtmap.setPaymentid(paymentinformation.getPaymentid());
		custpmtmap.setPaymenttype(paymentinformation.getPaymentType().toString());
		custpmtmap.setPaymentalias(paymentinformation.getAlias());
		custpmtmap.setIsDefault(paymentinformation.getIsDefault());

		custpmtmap.save();
	}

	public void setCustaccts(
			List<CustAcctMapDAO> custaccts) {
		this.custaccts = custaccts;
	}

	public void setCustpmttypes(
			List<CustPmtMap> custpmttypes) {
		this.custpmttypes = custpmttypes;
	}

	public void setDeviceList(
			List<Device> deviceList) {
		this.deviceList = deviceList;
	}

	public CustTopUp setTopupAmount(
			Account account,
			String topupAmount) throws CustomerException {
		if (id == 0) {
			throw new CustomerException("setTopupAmount", "Customer.id must be set");
		}
		CustTopUp custTopUp = new CustTopUp();
		custTopUp.setCustid(id);
		custTopUp.setTopupAmount(topupAmount);
		custTopUp.setAccountNo(account.getAccountNo());
		custTopUp.save();
		return getTopupAmount(account);
	}

	public PaymentUnitResponse submitPayment(
			PaymentTransaction transaction,
			int paymentId) throws PaymentException {
		PaymentUnitResponse retValue = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		System.out.println("Payment Amount " + transaction.getPaymentAmount());
		Query q = session.getNamedQuery("sbt_pmt_info");
		q.setParameter("in_cust_id", id);
		q.setParameter("in_pmt_id", paymentId);
		q.setParameter("in_pymntamt", transaction.getPaymentAmount());

		List<PaymentUnitResponse> responseList = q.list();
		for (PaymentUnitResponse response : responseList) {
			retValue = response;
		}

		session.getTransaction().commit();
		return retValue;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Customer Object....");
		sb.append("\n");
		sb.append("id               :: " + id);
		return sb.toString();
	}

	public CreditCard updateCreditCardPaymentInformation(
			CreditCard creditcard) throws PaymentException {
		if (id <= 0) {
			throw new CustomerException("updateCreditCardPaymentInformation", "Please specify a Customer Id");
		}
		if (creditcard == null || creditcard.getPaymentid() <= 0) {
			throw new PaymentException("updateCreditCardPaymentInformation", "Please specify payment information to update.");
		}
		/*
		 * we dont need to validate CC since it's an update. we're assuming the client will handle this and null fields will
		 * not be updated anyways
		 */
		if (true /* creditcard.validate() */) {
			if (creditcard.getAlias() == null || creditcard.getAlias().trim().length() == 0) {
				int myFirstCardNumber = Integer.parseInt(creditcard.getCreditCardNumber().substring(0, 1));
				String myAlias = " "
						+ creditcard.getCreditCardNumber().substring(creditcard.getCreditCardNumber().length() - 4, creditcard.getCreditCardNumber().length());
				switch (myFirstCardNumber) {
					case 3:
						myAlias = "AMEX" + myAlias;
						break;
					case 4:
						myAlias = "VISA" + myAlias;
						break;
					case 5:
						myAlias = "MasterCard" + myAlias;
						break;
					case 6:
						myAlias = "Discover" + myAlias;
						break;
				}
				creditcard.setAlias(myAlias);
			}
			creditcard.savePaymentOption();
		}
		return creditcard;
	}

}
