package com.tscp.mvne.refund;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import com.tscp.mvne.billing.Account;
import com.tscp.mvne.billing.contract.exception.ContractException;
import com.tscp.mvne.exception.DaoException;
import com.tscp.mvne.hibernate.GeneralSPResponse;
import com.tscp.mvne.hibernate.HibernateUtil;
import com.tscp.mvne.payment.dao.CreditCard;

@SuppressWarnings("unchecked")
public class RefundDao {
 
  public static void main(String[] args){
	  RefundDao dao = new RefundDao();
	  try{
		  
	     //dao.refundPayment(757620, 6875300, 5723215, "10", "wotg", 1, "test sp");
	     dao.refundPayment(757617, 6875300, 5723214, "10", "wotg", 1, "test sp");
	     //System.out.println(dao.isRefunded(687535));
	  }
	  catch(Exception e){
		  e.printStackTrace();
	  }
  }
  
  public int refundPayment(int accountNo, int transId, int trackingId, String amount, String refundBy, int refundCode, String notes) throws RefundException {
	    if(isRefunded(transId)) {
	       throw new RefundException("Refund was alread applied for the transaction with transId = "+ transId);
	    } 	   
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    Transaction transaction = session.beginTransaction();
	    List<GeneralSPResponse> list = null;
	    try {
	      Query query = session.getNamedQuery("sp_refund_pmt");
	      query.setParameter("in_account_no", accountNo);
	      query.setParameter("in_tracking_id", trackingId);
	      query.setParameter("in_refund_amount", amount);
	      query.setParameter("in_refund_by", refundBy);
	      query.setParameter("in_refund_code", refundCode);
	      query.setParameter("in_refund_notes", notes);
	      list = query.list();
	      
	      GeneralSPResponse response = getQueryResponse(list, transaction, session);
	      if(response != null && response.isSuccess() && !transaction.wasCommitted()) {
		     transaction.commit();
		     return response.getCode();
		  } else {
		     throw new RefundException("Error refund payment " + "Fail Reason is : "  + response.getMsg());
		  }
	      
	    }catch(HibernateException he) {
	    	if(transaction != null && !transaction.wasRolledBack())
	           transaction.rollback(); 
	        he.printStackTrace();
	        throw new RefundException("applyChargeCredit", he.getMessage());
	    }finally {
	       if(session.isOpen()) {
	          try {
	            session.close();
	          }catch (HibernateException e) {
	            e.printStackTrace();
	          }
	       }
	    }  
  }
  
  private static GeneralSPResponse getQueryResponse(List cursor, Transaction transaction, Session session) {
	    GeneralSPResponse response = null;
	    if (cursor == null || cursor.size() < 1) {
	        if(!transaction.wasRolledBack())
	    	   transaction.rollback();
	        if(session.isOpen())
	           session.close();
	        response = new GeneralSPResponse();
	        response.setStatus("N");
	        response.setMsg("No items returned in cursor");
	    } else {
	      response = (GeneralSPResponse) cursor.get(0);
	    }
	    return response;
	  }
  
  private boolean isRefunded(int transId){
	  Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	  Refund refund = getRefundByTransId(session, transId);
	  return (refund != null);
  }
  
  private Refund getRefundByTransId(Session session, int transId){
	  Transaction transaction = session.beginTransaction();
	  Refund refund = null;
	  try {
	      refund = (Refund)session.get(Refund.class, transId);
	      if(!transaction.wasCommitted())
	    	 transaction.commit(); 
	  }
	  catch (HibernateException he){
		  if(!transaction.wasRolledBack())
			  transaction.rollback();
	      he.printStackTrace();
	  }
      finally {
        if(session.isOpen()) {
           try {
              session.close();
           }
           catch (HibernateException e) {
             e.printStackTrace();
           }
        }
     }  
     return refund;	  
  }
}