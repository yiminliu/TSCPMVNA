package com.tscp.mvne.refund;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import com.tscp.mvne.billing.Account;
import com.tscp.mvne.billing.contract.exception.ContractException;
import com.tscp.mvne.hibernate.GeneralSPResponse;
import com.tscp.mvne.hibernate.HibernateUtil;
import com.tscp.mvne.payment.dao.CreditCard;

@SuppressWarnings("unchecked")
public class KenanPaymentDao {

  public static List<KenanPayment> getKenanPayments(Account account) throws RefundException {
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    Transaction transaction = session.beginTransaction();
    try {
      Query query = session.getNamedQuery("get_kenan_payments");
      query.setParameter("in_account_no", account.getAccountNo());
      List<KenanPayment> payments = query.list();
      transaction.commit();
      return payments;
    } catch (RuntimeException e) {
      HibernateUtil.rollbackTransaction(transaction);
      throw new RefundException("Error fetching Kenan Payments (getKenanPayments)");
    }
  }

  public static void reversePayment(Account account, String amount, Date transDate, String trackingId) throws RefundException {
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    Transaction transaction = session.beginTransaction();

    Query query = session.getNamedQuery("ins_payment_reversal");
    query.setParameter("in_account_no", account.getAccountNo());
    query.setParameter("in_reversal_amount", amount);
    query.setParameter("in_trans_date", transDate);
    query.setParameter("in_tracking_id", trackingId);
    List<GeneralSPResponse> list = query.list();

    if (list == null) {
      transaction.rollback();
      session.close();
      throw new ContractException("reversePayment", "Error reversing payment " + trackingId + " on account " + account.getAccountNo());
    } else {
      if (list.size() > 0) {
        if (!list.get(0).getStatus().equals("Y")) {
          transaction.rollback();
          throw new ContractException("reversePayment", "Error reversing payment " + trackingId + " on account " + account.getAccountNo()
              + ". Fail Reason is : " + list.get(0).getMsg());
        } else {
          transaction.commit();
        }
      } else {
        transaction.rollback();
        throw new ContractException("reversePayment", "Error reversing payment. No cursor items returned...");
      }
    }
  }

  public static void applyChargeCredit(CreditCard creditCard, String amount) {
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    Transaction transaction = session.beginTransaction();

    String address1 = creditCard.getAddress1();
    String address2 = creditCard.getAddress2();
    String address;
    if (address2 == null || address2.isEmpty()) {
      address = address1;
    } else {
      address = address1 + " " + address2;
    }

    Query query = session.getNamedQuery("truconnect_pccharge_credit");
    query.setParameter("in_cardno", creditCard.getCreditCardNumber());
    query.setParameter("in_cardexpdt", creditCard.getExpirationDate());
    query.setParameter("in_seccode", creditCard.getVerificationcode());
    query.setParameter("in_pymtamt", amount);
    query.setParameter("in_zip", creditCard.getZip());
    query.setParameter("in_cardholder", creditCard.getNameOnCreditCard());
    query.setParameter("in_street", address);
    List<GeneralSPResponse> list = query.list();

    if (list == null) {
      transaction.rollback();
      session.close();
      throw new ContractException("applyChargeCredit", "Error applying pccharge credit on card " + creditCard.getNameOnCreditCard() + " "
          + creditCard.getCreditCardNumber());
    } else {
      if (list.size() > 0) {
        if (!list.get(0).getStatus().equals("Y")) {
          transaction.rollback();
          throw new ContractException("applyChargeCredit", "Error applying pccharge credit on card " + creditCard.getNameOnCreditCard() + " "
              + creditCard.getCreditCardNumber() + ". Fail Reason is : " + list.get(0).getMsg());
        } else {
          transaction.commit();
        }
      } else {
        transaction.rollback();
        throw new ContractException("applyChargeCredit", "Error applying pccharge credit. No cursor items returned...");
      }
    }
  }

  public static void applyChargeCredit(int accountNo, int trackingId, String amount, String refundBy, int refundCode, String notes) throws RefundException {
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    Transaction transaction = session.beginTransaction();
    try {
      Query query = session.getNamedQuery("sp_refund_pmt");
      query.setParameter("in_account_no", accountNo);
      query.setParameter("in_tracking_id", trackingId);
      query.setParameter("in_refund_amount", amount);
      query.setParameter("in_refund_by", refundBy);
      query.setParameter("in_refund_reason_code", refundCode);
      query.setParameter("in_refund_notes", notes);
      List<GeneralSPResponse> list = query.list();
      if (list == null) {
        transaction.rollback();
        session.close();
        throw new ContractException("applyChargeCredit", "Error applying pccharge credit. No cursor items returned...");
      } else if (list.size() > 0) {
        if (!list.get(0).getStatus().equals("Y")) {
          transaction.rollback();
          session.close();
          throw new ContractException("applyChargeCredit", "Error applying pccharge credit on card " + ". Fail Reason is : " + list.get(0).getMsg());
        } else {
          transaction.commit();
        }
      } else {
        transaction.rollback();
        throw new ContractException("applyChargeCredit", "Error applying pccharge credit. No cursor items returned...");
      }
    } catch (HibernateException he) {
      transaction.rollback();
      throw new RefundException("applyChargeCredit", he.getMessage());
    } finally {
      if (session.isOpen()) {
        try {
          session.close();
        } catch (HibernateException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
