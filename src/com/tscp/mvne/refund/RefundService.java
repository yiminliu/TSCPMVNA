package com.tscp.mvne.refund;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tscp.mvne.billing.Account;
import com.tscp.mvne.hibernate.HibernateUtil;
import com.tscp.mvne.payment.PaymentException;
import com.tscp.mvne.payment.dao.CreditCard;
import com.tscp.mvne.payment.dao.PaymentTransaction;

public class RefundService {

  public List<KenanPayment> getKenanPayments(Account account) throws RefundException {
    return KenanPaymentDao.getKenanPayments(account);
  }

  public void reversePayment(Account account, String amount, Date transDate, String trackingId) throws RefundException {
    KenanPaymentDao.reversePayment(account, amount, transDate, trackingId);
  }

  public void applyChargeCredit(CreditCard creditCard, String amount) throws RefundException {
    KenanPaymentDao.applyChargeCredit(creditCard, amount);
  }

  public void applyChargeCredit(int accountNo, int trackingId, String amount, String refundBy) throws RefundException {
    KenanPaymentDao.applyChargeCredit(accountNo, trackingId, amount, refundBy);
  }

  public PaymentTransaction getPaymentTransaction(int custId, int transId) throws PaymentException {
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();
    Query q;
    q = session.getNamedQuery("fetch_pmt_trans");
    q.setParameter("in_cust_id", custId);
    q.setParameter("in_trans_id", transId);
    List<PaymentTransaction> paymentTransactions = q.list();
    session.getTransaction().rollback();
    if (paymentTransactions != null && !paymentTransactions.isEmpty()) {
      if (paymentTransactions.size() == 1) {
        return paymentTransactions.get(0);
      } else {
        throw new PaymentException("More than one payment transaction found with transId " + transId + " for cust " + custId);
      }
    } else {
      throw new PaymentException("No payment transaction found with transId " + transId + " for cust " + custId);
    }
  }

}
