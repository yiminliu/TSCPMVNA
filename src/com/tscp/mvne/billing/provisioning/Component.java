package com.tscp.mvne.billing.provisioning;

import java.util.Date;

import org.joda.time.DateTime;

// TODO jpong: Change property names to match java convention. This will require hibernate re-mapping. This needs to be done for all ORM objects.
public class Component {
  private int id = 0;
  private int instanceId;
  private int elementId;
  private String name;
  private DateTime activeDate;
  private DateTime inactiveDate;

  public Component() {
    // do nothing
  }

  public Component(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(int instanceId) {
    this.instanceId = instanceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getElementId() {
    return elementId;
  }

  public void setElementId(int elementId) {
    this.elementId = elementId;
  }

  public DateTime getActiveDate() {
    return activeDate;
  }

  public void setActiveDate(DateTime activeDate) {
    this.activeDate = activeDate;
  }

  public DateTime getInactiveDate() {
    return inactiveDate;
  }

  public void setInactiveDate(DateTime inactiveDate) {
    this.inactiveDate = inactiveDate;
  }

  @Override
  public String toString() {
    return "Component [component_id=" + id + ", component_instance_id=" + instanceId + ", component_name=" + name + ", element_id=" + elementId
        + ", active_date=" + activeDate + ", inactive_date=" + inactiveDate + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((activeDate == null) ? 0 : activeDate.hashCode());
    result = prime * result + elementId;
    result = prime * result + id;
    result = prime * result + ((inactiveDate == null) ? 0 : inactiveDate.hashCode());
    result = prime * result + instanceId;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Component other = (Component) obj;
    // if (activeDate == null) {
    // if (other.activeDate != null)
    // return false;
    // } else if (!activeDate.equals(other.activeDate))
    // return false;
    if (elementId != other.elementId)
      return false;
    if (id != other.id)
      return false;
    // if (inactiveDate == null) {
    // if (other.inactiveDate != null)
    // return false;
    // } else if (!inactiveDate.equals(other.inactiveDate))
    // return false;
    if (instanceId != other.instanceId)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}