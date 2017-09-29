package com.chickling.kmanager.jmx.metrics;

/**
 * @author Hulva Luva.H from ECBD
 * @since 2017-07-26
 * 
 *        RequestMetrics
 */
public class MetricType4 {
  private long count;
  private double max;
  private double mean;
  private double min;
  private double stdDev; // 标准差
  private double p50thPercentile;
  private double p75thPercentile;
  private double p95thPercentile;
  private double p98thPercentile;
  private double p99thPercentile;
  private double p999thPercentile;

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public double getMax() {
    return max;
  }

  public void setMax(double max) {
    this.max = max;
  }

  public double getMean() {
    return mean;
  }

  public void setMean(double mean) {
    this.mean = mean;
  }

  public double getMin() {
    return min;
  }

  public void setMin(double min) {
    this.min = min;
  }

  public double getStdDev() {
    return stdDev;
  }

  public void setStdDev(double stdDev) {
    this.stdDev = stdDev;
  }

  public double getP50thPercentile() {
    return p50thPercentile;
  }

  public void setP50thPercentile(double p50thPercentile) {
    this.p50thPercentile = p50thPercentile;
  }

  public double getP75thPercentile() {
    return p75thPercentile;
  }

  public void setP75thPercentile(double p75thPercentile) {
    this.p75thPercentile = p75thPercentile;
  }

  public double getP95thPercentile() {
    return p95thPercentile;
  }

  public void setP95thPercentile(double p95thPercentile) {
    this.p95thPercentile = p95thPercentile;
  }

  public double getP98thPercentile() {
    return p98thPercentile;
  }

  public void setP98thPercentile(double p98thPercentile) {
    this.p98thPercentile = p98thPercentile;
  }

  public double getP99thPercentile() {
    return p99thPercentile;
  }

  public void setP99thPercentile(double p99thPercentile) {
    this.p99thPercentile = p99thPercentile;
  }

  public double getP999thPercentile() {
    return p999thPercentile;
  }

  public void setP999thPercentile(double p999thPercentile) {
    this.p999thPercentile = p999thPercentile;
  }

}
