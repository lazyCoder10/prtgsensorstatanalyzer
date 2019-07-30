package com.mangoca.gp.model;

public class GpServiceModel {
    private String serviceName;
    private String serviceIp;
    private String maximumLatency;
    private String minimumLatency;
    private String averageLatency;
    private String pingTime;
    private String packetLoss;

    public String getMaximumLatency() {
        return maximumLatency;
    }

    public void setMaximumLatency(String maximumLatency) {
        this.maximumLatency = maximumLatency;
    }

    public String getMinimumLatency() {
        return minimumLatency;
    }

    public void setMinimumLatency(String minimumLatency) {
        this.minimumLatency = minimumLatency;
    }

    public String getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(String averageLatency) {
        this.averageLatency = averageLatency;
    }

    public String getPingTime() {
        return pingTime;
    }

    public void setPingTime(String pingTime) {
        this.pingTime = pingTime;
    }

    public String getPacketLoss() {
        return packetLoss;
    }

    public void setPacketLoss(String packetLoss) {
        this.packetLoss = packetLoss;
    }
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceIp() {
        return serviceIp;
    }

    public void setServiceIp(String serviceIp) {
        this.serviceIp = serviceIp;
    }


}
