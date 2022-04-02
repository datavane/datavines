package io.datavines.common.entity;

/**
 * 
 */
public class ActiveNodeInfo {

    private String ip;
    private int rpcPort;
    private int httpPort;

    public ActiveNodeInfo() {
    }

    public ActiveNodeInfo(String ip, int rpcPort, int httpPort) {
        this.ip = ip;
        this.rpcPort = rpcPort;
        this.httpPort = httpPort;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }
}
