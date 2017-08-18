package com.chickling.kmanager.jmx.metrics;

/**
 * @author Hulva Luva.H
 * @since 2017-07-26
 *
 *        kafka.server:type=replica-fetcher-metrics
 * 
 *        kafka.server:type=socket-server-metrics
 */
public class MetricType3 {
  private double connectionCloseRate;
  private double connectionCount;
  private double connectionCreationRate;
  private double incomingByteRate;
  private double ioRatio;
  private double idTimeNSavg;
  private double ioWaitTimeNSavg;
  private double networkIOrate;
  private double outgoingByteRate;
  private double requestRate;
  private double requestSizeAvg;
  private double requestSizeMax;
  private double responseRate;
  private double selectRate;

  public double getConnectionCloseRate() {
    return connectionCloseRate;
  }

  public void setConnectionCloseRate(double connectionCloseRate) {
    this.connectionCloseRate = connectionCloseRate;
  }

  public double getConnectionCount() {
    return connectionCount;
  }

  public void setConnectionCount(double connectionCount) {
    this.connectionCount = connectionCount;
  }

  public double getConnectionCreationRate() {
    return connectionCreationRate;
  }

  public void setConnectionCreationRate(double connectionCreationRate) {
    this.connectionCreationRate = connectionCreationRate;
  }

  public double getIncomingByteRate() {
    return incomingByteRate;
  }

  public void setIncomingByteRate(double incomingByteRate) {
    this.incomingByteRate = incomingByteRate;
  }

  public double getIoRatio() {
    return ioRatio;
  }

  public void setIoRatio(double ioRatio) {
    this.ioRatio = ioRatio;
  }

  public double getIdTimeNSavg() {
    return idTimeNSavg;
  }

  public void setIdTimeNSavg(double idTimeNSavg) {
    this.idTimeNSavg = idTimeNSavg;
  }

  public double getIoWaitTimeNSavg() {
    return ioWaitTimeNSavg;
  }

  public void setIoWaitTimeNSavg(double ioWaitTimeNSavg) {
    this.ioWaitTimeNSavg = ioWaitTimeNSavg;
  }

  public double getNetworkIOrate() {
    return networkIOrate;
  }

  public void setNetworkIOrate(double networkIOrate) {
    this.networkIOrate = networkIOrate;
  }

  public double getOutgoingByteRate() {
    return outgoingByteRate;
  }

  public void setOutgoingByteRate(double outgoingByteRate) {
    this.outgoingByteRate = outgoingByteRate;
  }

  public double getRequestRate() {
    return requestRate;
  }

  public void setRequestRate(double requestRate) {
    this.requestRate = requestRate;
  }

  public double getRequestSizeAvg() {
    return requestSizeAvg;
  }

  public void setRequestSizeAvg(double requestSizeAvg) {
    this.requestSizeAvg = requestSizeAvg;
  }

  public double getRequestSizeMax() {
    return requestSizeMax;
  }

  public void setRequestSizeMax(double requestSizeMax) {
    this.requestSizeMax = requestSizeMax;
  }

  public double getResponseRate() {
    return responseRate;
  }

  public void setResponseRate(double responseRate) {
    this.responseRate = responseRate;
  }

  public double getSelectRate() {
    return selectRate;
  }

  public void setSelectRate(double selectRate) {
    this.selectRate = selectRate;
  }

}
