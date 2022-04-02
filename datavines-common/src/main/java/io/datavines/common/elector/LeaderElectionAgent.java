package io.datavines.common.elector;

public class LeaderElectionAgent {

    protected LeaderElectable leaderElector;

    public void stop(){}

    public void start(){}

    public LeaderElectable getLeaderElector(){
        return leaderElector;
    }

    public void setLeaderElector(LeaderElectable leaderElector){
        this.leaderElector = leaderElector;
    }
}
