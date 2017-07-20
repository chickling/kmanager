package com.chickling.kmonitor.core.jmx.metric;

import com.chickling.kmonitor.utils.MetricUtils;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class OSMetric {

	private Double processCpuLoad;
	private Double systemCpuLoad;

	public OSMetric() {
		super();
	}

	public OSMetric(Double processCpuLoad, Double systemCpuLoad) {
		super();
		this.processCpuLoad = processCpuLoad;
		this.systemCpuLoad = systemCpuLoad;
	}

	public Double getProcessCpuLoad() {
		return processCpuLoad;
	}

	public void setProcessCpuLoad(Double processCpuLoad) {
		this.processCpuLoad = processCpuLoad;
	}

	public Double getSystemCpuLoad() {
		return systemCpuLoad;
	}

	public void setSystemCpuLoad(Double systemCpuLoad) {
		this.systemCpuLoad = systemCpuLoad;
	}

	public String formatedProcessCpuLoad() {
		return MetricUtils.rateFormat(getProcessCpuLoad(), 0);
	}

	public String formatedSystemCpuLoad() {
		return MetricUtils.rateFormat(getSystemCpuLoad(), 0);
	}

	@Override
	public String toString() {
		return "OSMetric [processCpuLoad=" + processCpuLoad + ", systemCpuLoad=" + systemCpuLoad + "]";
	}

}
