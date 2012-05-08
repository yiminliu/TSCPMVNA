package com.tscp.mvne.device;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tscp.mvne.customer.DeviceException;
import com.tscp.mvne.exception.DaoException;
import com.tscp.mvne.hibernate.GeneralSPResponse;
import com.tscp.mvne.hibernate.HibernateUtil;
import com.tscp.mvne.hibernate.ResponseReader;

public class DeviceAssociationDao {

  public static void save(DeviceAssociation deviceAssociation) throws DeviceException {
    validate(deviceAssociation);
    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    session.beginTransaction();

    Query q = session.getNamedQuery("ins_device_assoc_map");
    q.setParameter("in_device_id", deviceAssociation.getDeviceId());
    q.setParameter("in_subscr_no", deviceAssociation.getSubscrNo());
    q.setParameter("in_status", deviceAssociation.getStatus());
    q.setParameter("in_value", deviceAssociation.getValue());

    @SuppressWarnings("unchecked")
    List<GeneralSPResponse> generalSPResponseList = q.list();

    try {
      ResponseReader.validateSuccess(generalSPResponseList);
      session.getTransaction().commit();
    } catch (DaoException e) {
      session.getTransaction().rollback();
      throw e;
    }

  }

  protected static void validate(DeviceAssociation deviceAssociation) throws DeviceException {
    if (deviceAssociation.getDeviceId() <= 0)
      throw new DeviceException("Device ID not set");
    if (deviceAssociation.getSubscrNo() <= 0)
      throw new DeviceException("Subscriber Number not set");
  }
}
