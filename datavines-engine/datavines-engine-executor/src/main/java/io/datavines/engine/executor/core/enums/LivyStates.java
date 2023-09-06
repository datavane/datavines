/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.engine.executor.core.enums;



import java.util.Map;


public class LivyStates {

    /**
     * UNKNOWN is used to represent the state that server get null from Livy.
     * the other state is just same as com.cloudera.livy.sessions.SessionState.
     */
    public enum State {
        NOT_STARTED,
        STARTING,
        RECOVERING,
        IDLE,
        RUNNING,
        BUSY,
        SHUTTING_DOWN,
        ERROR,
        DEAD,
        KILLED,
        SUCCESS,
        UNKNOWN,
        STOPPED,
        FINDING,
        NOT_FOUND,
        FOUND
    }


    public static State toLivyState(Map<String, Object> object) {
        if (object != null) {
            Object state = object.get("state");
            Object finalStatus = object.get("finalStatus");

            State finalState = null;
            if (state != null) {
                finalState = State.valueOf(state.toString().toUpperCase());
            }
            return finalState != null ? finalState : State.valueOf(finalStatus.toString().toUpperCase());
        }
        return State.UNKNOWN;
    }


    public static boolean isActive(State state) {
        if (State.UNKNOWN.equals(state) || State.STOPPED.equals(state) || State.NOT_FOUND.equals
                (state) || State.FOUND.equals(state)) {
            // set UNKNOWN isActive() as false.
            return false;
        } else {
            return true;
        }
    }

    public static boolean isHealthy(State state) {
        return !(State.ERROR.equals(state) || State.DEAD.equals(state)
                || State.SHUTTING_DOWN.equals(state)
                || State.FINDING.equals(state)
                || State.NOT_FOUND.equals(state)
                || State.FOUND.equals(state));
    }
}
