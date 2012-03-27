package com.tscp.mvne.config;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.InternetAddress;

public class NOTIFICATION {
  public static final InternetAddress from = buildFrom();
  public static final Set<InternetAddress> bccList = buildBccList();

  protected static InternetAddress buildFrom() {
    try {
      return new InternetAddress("no-reply@truconnect.com", "TruConnect");
    } catch (UnsupportedEncodingException e) {
      return new InternetAddress();
    }
  }

  protected static Set<InternetAddress> buildBccList() {
    Set<InternetAddress> bccList = new HashSet<InternetAddress>();
    try {
      bccList.add(new InternetAddress("trualert@telscape.net", "TruAlert"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return bccList;
  }
}
