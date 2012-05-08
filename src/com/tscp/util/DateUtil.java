package com.tscp.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtil {
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
}
