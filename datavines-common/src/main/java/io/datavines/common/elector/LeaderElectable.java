package io.datavines.common.elector;

public interface LeaderElectable {
    /**
     * 已经选举为leader
     */
    void electedLeader();

    /**
     * 撤销自己的leader身份
     */
    void revokedLeadership();

    /**
     * 是否可用
     * @return boolean
     */
    boolean isActive();

    public void await();
}
