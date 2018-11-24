package com.chickling.kmanager.jmx;

import com.chickling.kmanager.utils.MetricUtils;

/**
 * @author Hulva Luva.H
 * @since 2017年7月20日
 *
 */
public class FormatedMeterMetric {
  private Long count;
  private String meanRate;
  private String oneMinuteRate;
  private String fiveMinuteRate;
  private String fifteenMinuteRate;

  public FormatedMeterMetric() {
    super();
  }

  public FormatedMeterMetric(MeterMetric metric) {
    this(metric.getCount(), MetricUtils.sizeFormat(metric.getMeanRate()), MetricUtils.sizeFormat(metric.getOneMinuteRate()),
        MetricUtils.sizeFormat(metric.getFiveMinuteRate()), MetricUtils.sizeFormat(metric.getFifteenMinuteRate()));
  }

  public FormatedMeterMetric(MeterMetric metric, int interation) {
    this(metric.getCount(), MetricUtils.rateFormat(metric.getMeanRate(), interation),
        MetricUtils.rateFormat(metric.getOneMinuteRate(), interation), MetricUtils.rateFormat(metric.getFiveMinuteRate(), interation),
        MetricUtils.rateFormat(metric.getFifteenMinuteRate(), interation));
  }

  public FormatedMeterMetric(Long count, String meanRate, String oneMinuteRate, String fiveMinuteRate, String fifteenMinuteRate) {
    super();
    this.count = count;
    this.meanRate = meanRate;
    this.oneMinuteRate = oneMinuteRate;
    this.fiveMinuteRate = fiveMinuteRate;
    this.fifteenMinuteRate = fifteenMinuteRate;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public String getFifteenMinuteRate() {
    return fifteenMinuteRate;
  }

  public void setFifteenMinuteRate(String fifteenMinuteRate) {
    this.fifteenMinuteRate = fifteenMinuteRate;
  }

  public String getFiveMinuteRate() {
    return fiveMinuteRate;
  }

  public void setFiveMinuteRate(String fiveMinuteRate) {
    this.fiveMinuteRate = fiveMinuteRate;
  }

  public String getOneMinuteRate() {
    return oneMinuteRate;
  }

  public void setOneMinuteRate(String oneMinuteRate) {
    this.oneMinuteRate = oneMinuteRate;
  }

  public String getMeanRate() {
    return meanRate;
  }

  public void setMeanRate(String meanRate) {
    this.meanRate = meanRate;
  }

}
