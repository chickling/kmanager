package com.chickling.kmonitor.core.jmx;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class MeterMetric {

	private Long count;
	private Double fifteenMinuteRate;
	private Double fiveMinuteRate;
	private Double oneMinuteRate;
	private Double meanRate;

	public MeterMetric() {
		super();
	}

	public MeterMetric(Long count, Double meanRate, Double oneMinuteRate, Double fiveMinuteRate,
			Double fifteenMinuteRate) {
		super();
		this.count = count;
		this.fifteenMinuteRate = fifteenMinuteRate;
		this.fiveMinuteRate = fiveMinuteRate;
		this.oneMinuteRate = oneMinuteRate;
		this.meanRate = meanRate;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Double getFifteenMinuteRate() {
		return fifteenMinuteRate;
	}

	public void setFifteenMinuteRate(Double fifteenMinuteRate) {
		this.fifteenMinuteRate = fifteenMinuteRate;
	}

	public Double getFiveMinuteRate() {
		return fiveMinuteRate;
	}

	public void setFiveMinuteRate(Double fiveMinuteRate) {
		this.fiveMinuteRate = fiveMinuteRate;
	}

	public Double getOneMinuteRate() {
		return oneMinuteRate;
	}

	public void setOneMinuteRate(Double oneMinuteRate) {
		this.oneMinuteRate = oneMinuteRate;
	}

	public Double getMeanRate() {
		return meanRate;
	}

	public void setMeanRate(Double meanRate) {
		this.meanRate = meanRate;
	}

	@Override
	public String toString() {
		return "MeterMrtric [count=" + count + ", fifteenMinuteRate=" + fifteenMinuteRate + ", fiveMinuteRate="
				+ fiveMinuteRate + ", oneMinuteRate=" + oneMinuteRate + ", meanRate=" + meanRate + "]";
	}

}
