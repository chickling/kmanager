package com.chickling.kmonitor.jmx.metrics;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-26
 * 
 */
public class MetricType1 {
  private long count;
  private String eventType;
  private double meanRate;
  private double oneMinuteRate;
  private double fiveMinuteRate;
  private double fifteenMinuteRate;
  private String rateUnit;

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public double getMeanRate() {
    return meanRate;
  }

  public void setMeanRate(double meanRate) {
    this.meanRate = meanRate;
  }

  public double getOneMinuteRate() {
    return oneMinuteRate;
  }

  public void setOneMinuteRate(double oneMinuteRate) {
    this.oneMinuteRate = oneMinuteRate;
  }

  public double getFiveMinuteRate() {
    return fiveMinuteRate;
  }

  public void setFiveMinuteRate(double fiveMinuteRate) {
    this.fiveMinuteRate = fiveMinuteRate;
  }

  public double getFifteenMinuteRate() {
    return fifteenMinuteRate;
  }

  public void setFifteenMinuteRate(double fifteenMinuteRate) {
    this.fifteenMinuteRate = fifteenMinuteRate;
  }

  public String getRateUnit() {
    return rateUnit;
  }

  public void setRateUnit(String rateUnit) {
    this.rateUnit = rateUnit;
  }

  @Override
  public String toString() {
    switch (this.eventType) {
      case "bytes":

        break;
      case "requests":

        break;
      case "percent":

        break;
      case "messages":

        break;
      case "expands": // IsrExpandsPerSec
        
        break;
      case "shrinks": // IsrShrinksPerSec
        
        break; 
      default:

        break;
    }
    return "MetricType1 [count=" + count + ", eventType=" + eventType + ", meanRate=" + meanRate + ", oneMinuteRate=" + oneMinuteRate
        + ", fiveMinuteRate=" + fiveMinuteRate + ", fifteenMinuteRate=" + fifteenMinuteRate + ", rateUnit=" + rateUnit + "]";
  }
}
