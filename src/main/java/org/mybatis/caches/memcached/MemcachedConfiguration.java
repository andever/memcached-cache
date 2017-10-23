/**
 *    Copyright 2012-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.caches.memcached;

import java.util.Arrays;


/**
 * The Memcached client configuration.
 *
 * @author Simone Tripodi
 */
final class MemcachedConfiguration {

    /**
     * The key prefix.
     */
    private String keyPrefix;

    /**
     * The Memcached servers.
     */
    private String[] servers;

    // initial, min and max pool sizes
    private int initConn;
    private int minConn;
    private int maxConn;
    private int maxIdle; // max idle time for avail sockets
    private long maxBusyTime; // max idle time for avail sockets
    private long maintSleep;
    private int socketTO; // default timeout of socket reads
    private int socketConnectTO; // default timeout of socket
    // connections
    // for being alive
    private boolean failover; // default to failover in event of cache
    // server dead
    private boolean failback; // only used if failover is also set ...
    // controls putting a dead server back
    // into rotation
    private boolean nagle; // enable/disable Nagle's algorithm
    private boolean aliveCheck; // disable health check of socket on checkout
    /**
     * The Memcached entries expiration time.
     */
    private int expiration;

    /**
     * @return the keyPrefix
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * @param keyPrefix the keyPrefix to set
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String[] getServers() {
        return servers;
    }

    public void setServers(String[] servers) {
        this.servers = servers;
    }

    public int getInitConn() {
        return initConn;
    }

    public void setInitConn(int initConn) {
        this.initConn = initConn;
    }

    public int getMinConn() {
        return minConn;
    }

    public void setMinConn(int minConn) {
        this.minConn = minConn;
    }

    public int getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public long getMaxBusyTime() {
        return maxBusyTime;
    }

    public void setMaxBusyTime(long maxBusyTime) {
        this.maxBusyTime = maxBusyTime;
    }

    public long getMaintSleep() {
        return maintSleep;
    }

    public void setMaintSleep(long maintSleep) {
        this.maintSleep = maintSleep;
    }

    public int getSocketTO() {
        return socketTO;
    }

    public void setSocketTO(int socketTO) {
        this.socketTO = socketTO;
    }

    public int getSocketConnectTO() {
        return socketConnectTO;
    }

    public void setSocketConnectTO(int socketConnectTO) {
        this.socketConnectTO = socketConnectTO;
    }

    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    public boolean isFailback() {
        return failback;
    }

    public void setFailback(boolean failback) {
        this.failback = failback;
    }

    public boolean isNagle() {
        return nagle;
    }

    public void setNagle(boolean nagle) {
        this.nagle = nagle;
    }

    public boolean isAliveCheck() {
        return aliveCheck;
    }

    public void setAliveCheck(boolean aliveCheck) {
        this.aliveCheck = aliveCheck;
    }

    /**
     * @return the expiration
     */
    public int getExpiration() {
        return expiration;
    }

    /**
     * @param expiration the expiration to set
     */
    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    @Override
    public String toString() {
        return "MemcachedConfiguration [keyPrefix=" + keyPrefix + ", servers=" + Arrays.toString(servers) + ", initConn=" + initConn + ", minConn="
                + minConn + ", maxConn=" + maxConn + ", maxIdle=" + maxIdle + ", maxBusyTime=" + maxBusyTime + ", maintSleep=" + maintSleep
                + ", socketTO=" + socketTO + ", socketConnectTO=" + socketConnectTO + ", failover=" + failover + ", failback=" + failback
                + ", nagle=" + nagle + ", aliveCheck=" + aliveCheck + ", expiration=" + expiration + "]";
    }

}
