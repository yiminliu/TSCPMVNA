package com.tscp.mvne.device;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tscp.mvne.customer.DeviceException;
import com.tscp.mvne.exception.DaoException;
import com.tscp.mvne.hibernate.GeneralSPResponse;
import com.tscp.mvne.hibernate.HibernateUtil;
import com.tscp.mvne.hibernate.ResponseReader;

public class DeviceDao {

	public static Device save(Device device) throws DeviceException {
		if (device.getCustId() <= 0) {
			throw new DeviceException("Customer Id has not been set");
		}
		if (device.getLabel() == null || device.getLabel().isEmpty()) {
			throw new DeviceException("Device Label cannot be empty");
		}
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query q;
		if (device.getId() == 0) {
			q = session.getNamedQuery("ins_device_info");
			q.setParameter("in_account_no", device.getAccountNo());
		} else {
			q = session.getNamedQuery("upd_device_info");
			q.setParameter("in_device_id", device.getId());
			q.setParameter("in_device_status_id", device.getStatusId());
			q.setParameter("in_eff_date", device.getEffectiveDate() == null ? "" : device.getEffectiveDate());
			q.setParameter("in_exp_date", device.getExpirationDate() == null ? "" : device.getExpirationDate());
		}
		q.setParameter("in_cust_id", device.getCustId());
		q.setParameter("in_device_label", device.getLabel());
		q.setParameter("in_device_value", device.getValue());

		@SuppressWarnings("unchecked")
		List<GeneralSPResponse> responseHolder = q.list();

		try {
			ResponseReader.validateSuccess(responseHolder);
			device.setId(ResponseReader.getValue(responseHolder));
			session.getTransaction().commit();
			return device;
		} catch (DaoException e) {
			session.getTransaction().rollback();
			throw e;
		}

	}

	public static void delete(Device device) {
		if (device.getId() <= 0) {
			throw new DeviceException("Device ID cannot be empty");
		}
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query q = session.getNamedQuery("del_device_info");
		q.setParameter("in_cust_id", device.getCustId());
		q.setParameter("in_device_id", device.getId());

		@SuppressWarnings("unchecked")
		List<GeneralSPResponse> responseHolder = q.list();

		try {
			ResponseReader.validateSuccess(responseHolder);
			session.getTransaction().commit();
		} catch (DaoException e) {
			session.getTransaction().rollback();
			throw e;
		}
	}
}
