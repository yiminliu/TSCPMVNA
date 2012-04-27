package com.tscp.mvne.device;

public enum DeviceStatus {
  UNKNOWN(0, "Unknown"), NEW(1, "New"), ACTIVE(2, "Active"), RELEASED(3, "Released / Reactivate-able"), REMOVED(4, "Released / Removed"), SUSPENDED(5,
      "Released / System-Reactivate"), BLOCKED(6, "Blocked");

  private int value;
  private String description;

  private DeviceStatus(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public int getValue() {
    return value;
  }
}