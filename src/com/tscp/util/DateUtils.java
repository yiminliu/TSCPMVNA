package com.tscp.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {
  protected static final DateTimeFormatter serviceInstance = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss aa");

  public static DateTime getServiceDateTime(String inDate) {
    if (inDate != null && !inDate.trim().isEmpty()) {
      return serviceInstance.parseDateTime(inDate);
    } else {
      return null;
    }
  }

  public static Date getServiceDate(String inDate) {
    if (inDate != null && !inDate.trim().isEmpty()) {
      return serviceInstance.parseDateTime(inDate).toDate();
    } else {
      return null;
    }
  }

  public static final XMLGregorianCalendar getXMLCalendar() {
    return getXMLCalendar(new Date());
  }

  public static final XMLGregorianCalendar getXMLCalendar(DateTime dateTime) {
    return getXMLCalendar(dateTime.toDate());
  }

  public static final XMLGregorianCalendar getXMLCalendar(Date date) {
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(date);
      XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
      return xmlDate;
    } catch (DatatypeConfigurationException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static final XMLGregorianCalendar getXMLCalendarNextDay() {
    DateTime dt = new DateTime();
    return getXMLCalendar(dt.plusDays(1).toDate());
  }

}
