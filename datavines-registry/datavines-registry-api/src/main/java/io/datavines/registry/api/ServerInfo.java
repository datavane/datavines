package io.datavines.registry.api;

public class ServerInfo {

    private String host;
    private Integer serverPort;

    public ServerInfo() {
    }

    public ServerInfo(String host, Integer serverPort) {
        this.host = host;
        this.serverPort = serverPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public String toString() {
        return host + ":" + serverPort;
    }
}
