package com.tscp.mvne.unittest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tscp.mvne.refund.RefundDao;

public class RefundTest {

	private RefundDao dao = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		dao = new RefundDao();
	}
	

	//@Test
	public void testRefundPayment() {
	   try{
	       dao.refundPayment(757617, 6875300, 5723214, "10", "wotg", 1, "test sp");			     
	   }
	   catch(Exception e){
		  e.printStackTrace();
	   }
	}

	@Test
	public void testIsRefunded() {
		assertTrue(dao.isRefunded(687535));
		assertTrue(dao.isRefunded(11111111));
	}

	@Test
	public void testGetRefundByTransId() {
		assertNotNull(dao.getRefundByTransId(757617));
	}

}
