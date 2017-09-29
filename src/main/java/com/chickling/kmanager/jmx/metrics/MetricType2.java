package com.chickling.kmanager.jmx.metrics;

/**
 * @author Hulva Luva.H
 * @since 2017-07-26
 *
 */
public class MetricType2<T> {
  private String name; // like queue-size Value byte-rate count
  private T value;
  private String unit;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

}
