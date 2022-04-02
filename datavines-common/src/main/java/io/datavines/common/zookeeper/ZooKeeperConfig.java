package io.datavines.common.zookeeper;

public class ZooKeeperConfig {

    private String serverList;

    private int baseSleepTimeMs = 100;

    private int maxSleepMs = 30000;

    private int maxRetries = 10;

    private int sessionTimeoutMs = 60000;

    private int connectionTimeoutMs = 30000;

    private String digest;

    public ZooKeeperConfig(final String serverList, final int baseSleepTimeMs, final int maxSleepMs, final int maxRetries) {
        this.serverList = serverList;
        this.baseSleepTimeMs = baseSleepTimeMs;
        this.maxSleepMs = maxSleepMs;
        this.maxRetries = maxRetries;
    }

    public ZooKeeperConfig(final String serverList) {
        this.serverList = serverList;
    }

    public ZooKeeperConfig() {
    }

    public String getServerList() {
        return serverList;
    }

    public void setServerList(String serverList) {
        this.serverList = serverList;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxSleepMs() {
        return maxSleepMs;
    }

    public void setMaxSleepMs(int maxSleepMs) {
        this.maxSleepMs = maxSleepMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}
