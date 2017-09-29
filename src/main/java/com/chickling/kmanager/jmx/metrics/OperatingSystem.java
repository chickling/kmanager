package com.chickling.kmanager.jmx.metrics;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-26
 *
 */
public class OperatingSystem {
  private String arch;
  private String name;
  private String version;

  private int availableProcessors;

  private int maxFileDescriptorCount; // TODO ?
  private int openFileDescriptorCount;

  private double processCpuLoad;
  private long processCpuTime;
  private double systemCpuLoad;
  private double systemLoadAverage;

  private long totalPhysicalMemorySize;
  private long freePhysicalMemorySize;
  private long committedVirtualMemorySize;
  private long totalSwapSpaceSize;
  private long freeSwapSpaceSize;

  public String getArch() {
    return arch;
  }

  public void setArch(String arch) {
    this.arch = arch;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getAvailableProcessors() {
    return availableProcessors;
  }

  public void setAvailableProcessors(int availableProcessors) {
    this.availableProcessors = availableProcessors;
  }

  public int getMaxFileDescriptorCount() {
    return maxFileDescriptorCount;
  }

  public void setMaxFileDescriptorCount(int maxFileDescriptorCount) {
    this.maxFileDescriptorCount = maxFileDescriptorCount;
  }

  public int getOpenFileDescriptorCount() {
    return openFileDescriptorCount;
  }

  public void setOpenFileDescriptorCount(int openFileDescriptorCount) {
    this.openFileDescriptorCount = openFileDescriptorCount;
  }

  public double getProcessCpuLoad() {
    return processCpuLoad;
  }

  public void setProcessCpuLoad(double processCpuLoad) {
    this.processCpuLoad = processCpuLoad;
  }

  public long getProcessCpuTime() {
    return processCpuTime;
  }

  public void setProcessCpuTime(long processCpuTime) {
    this.processCpuTime = processCpuTime;
  }

  public double getSystemCpuLoad() {
    return systemCpuLoad;
  }

  public void setSystemCpuLoad(double systemCpuLoad) {
    this.systemCpuLoad = systemCpuLoad;
  }

  public double getSystemLoadAverage() {
    return systemLoadAverage;
  }

  public void setSystemLoadAverage(double systemLoadAverage) {
    this.systemLoadAverage = systemLoadAverage;
  }

  public long getTotalPhysicalMemorySize() {
    return totalPhysicalMemorySize;
  }

  public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
    this.totalPhysicalMemorySize = totalPhysicalMemorySize;
  }

  public long getFreePhysicalMemorySize() {
    return freePhysicalMemorySize;
  }

  public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
    this.freePhysicalMemorySize = freePhysicalMemorySize;
  }

  public long getCommittedVirtualMemorySize() {
    return committedVirtualMemorySize;
  }

  public void setCommittedVirtualMemorySize(long committedVirtualMemorySize) {
    this.committedVirtualMemorySize = committedVirtualMemorySize;
  }

  public long getTotalSwapSpaceSize() {
    return totalSwapSpaceSize;
  }

  public void setTotalSwapSpaceSize(long totalSwapSpaceSize) {
    this.totalSwapSpaceSize = totalSwapSpaceSize;
  }

  public long getFreeSwapSpaceSize() {
    return freeSwapSpaceSize;
  }

  public void setFreeSwapSpaceSize(long freeSwapSpaceSize) {
    this.freeSwapSpaceSize = freeSwapSpaceSize;
  }

  @Override
  public String toString() {
    return "OperatingSystem [arch=" + arch + ", name=" + name + ", version=" + version + ", availableProcessors=" + availableProcessors
        + ", maxFileDescriptorCount=" + maxFileDescriptorCount + ", openFileDescriptorCount=" + openFileDescriptorCount
        + ", processCpuLoad=" + processCpuLoad + ", processCpuTime=" + processCpuTime + ", systemCpuLoad=" + systemCpuLoad
        + ", systemLoadAverage=" + systemLoadAverage + ", totalPhysicalMemorySize=" + totalPhysicalMemorySize + ", freePhysicalMemorySize="
        + freePhysicalMemorySize + ", committedVirtualMemorySize=" + committedVirtualMemorySize + ", totalSwapSpaceSize="
        + totalSwapSpaceSize + ", freeSwapSpaceSize=" + freeSwapSpaceSize + "]";
  }

}
