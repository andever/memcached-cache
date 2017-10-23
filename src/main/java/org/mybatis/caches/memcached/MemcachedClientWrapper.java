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

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import com.schooner.MemCached.BinaryClient;
import com.schooner.MemCached.MemcachedItem;
import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

/**
 * @author Simone Tripodi
 */
final class MemcachedClientWrapper {

    /**
     * This class log.
     */
    private static final Log LOG = LogFactory.getLog(MemcachedCache.class);

    private final MemcachedConfiguration configuration;

    private final MemCachedClient client;

    static {
        MemcachedConfiguration config = MemcachedConfigurationBuilder.getInstance().parseConfiguration();
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(config.getServers());
        pool.setInitConn(config.getInitConn());
        pool.setMinConn(config.getMinConn());
        pool.setMaxConn(config.getMaxConn());
        pool.setMaxIdle(config.getMaxIdle());
        pool.setMaxBusyTime(config.getMaxBusyTime());
        pool.setMaintSleep(config.getMaintSleep());
        pool.setSocketTO(config.getSocketTO());
        pool.setSocketConnectTO(config.getSocketConnectTO());
        pool.setNagle(config.isNagle());
        pool.setFailback(config.isFailback());
        pool.setFailover(config.isFailover());
        pool.setHashingAlg(SockIOPool.NEW_COMPAT_HASH);
        pool.setAliveCheck(config.isAliveCheck());
        pool.initialize();
    }
	/**
	 * Used to represent an object retrieved from Memcached along with its CAS information
	 * 
	 * @author Weisz, Gustavo E.
	 */
	private class ObjectWithCas {

		Object object;
		long cas;

		ObjectWithCas(Object object, long cas) {
			this.setObject(object);
			this.setCas(cas);
		}

		public Object getObject() {
			return object;
		}

		public void setObject(Object object) {
			this.object = object;
		}

		public long getCas() {
			return cas;
		}

		public void setCas(long cas) {
			this.cas = cas;
		}

	}

    public MemcachedClientWrapper() {
        configuration = MemcachedConfigurationBuilder.getInstance().parseConfiguration();
        client = new BinaryClient();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Running new Memcached client using " + configuration);
        }
    }

    /**
     * Converts the MyBatis object key in the proper string representation.
     * 
     * @param key the MyBatis object key.
     * @return the proper string representation.
     */
    private String toKeyString(final Object key) {
        // issue #1, key too long
        String keyString = configuration.getKeyPrefix() + StringUtils.sha1Hex(key.toString());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Object key '"
                    + key
                    + "' converted in '"
                    + keyString
                    + "'");
        }
        return keyString;
    }

    /**
     *
     * @param key
     * @return
     */
    public Object getObject(Object key) {
        String keyString = toKeyString(key);
        Object ret = retrieve(keyString);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrived object ("
                    + keyString
                    + ", "
                    + ret
                    + ")");
        }

        return ret;
    }

	/**
	 * Return the stored group in Memcached identified by the specified key.
	 *
	 * @param groupKey
	 *            the group key.
	 * @return the group if was previously stored, null otherwise.
	 */
	private ObjectWithCas getGroup(String groupKey) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Retrieving group with id '" + groupKey + "'");
		}

		ObjectWithCas groups = null;
		try {
			groups = retrieveWithCas(groupKey);
		} catch (Exception e) {
			LOG.error("Impossible to retrieve group '" + groupKey + "' see nested exceptions", e);
		}

		if (groups == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Group '" + groupKey + "' not previously stored");
			}
			return null;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("retrieved group '" + groupKey + "' with values " + groups);
		}

		return groups;
	}

    /**
     *
     *
     * @param keyString
     * @return
     * @throws Exception
     */
    private Object retrieve(final String keyString) {
        Object retrieved = client.get(keyString);
        return retrieved;
    }

	/**
	 * Retrieves an object along with its cas using the given key
	 * 
	 * @param keyString
	 * @return
	 * @throws Exception
	 */
	private ObjectWithCas retrieveWithCas(final String keyString) {
		MemcachedItem retrieved = client.gets(keyString);
		if (retrieved == null) {
			return null;
		}

		return new ObjectWithCas(retrieved.getValue(), retrieved.getCasUnique());
	}

    @SuppressWarnings("unchecked")
	public void putObject(Object key, Object value, String id) {
        String keyString = toKeyString(key);
        String groupKey = toKeyString(id);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Putting object ("
                    + keyString
                    + ", "
                    + value
                    + ")");
        }

        storeInMemcached(keyString, value);

        // add namespace key into memcached
        // Optimistic lock approach...
 		boolean jobDone = false;

 		while (!jobDone) {
 			ObjectWithCas group = getGroup(groupKey);
 			Set<String> groupValues;

 			if (group == null || group.getObject() == null) {
 				groupValues = new HashSet<String>();
 				groupValues.add(keyString);

 				if (LOG.isDebugEnabled()) {
 					LOG.debug("Insert/Updating object (" + groupKey + ", " + groupValues + ")");
 				}

 				jobDone = tryToAdd(groupKey, groupValues);
 			} else {
 				groupValues = (Set<String>) group.getObject();
 				groupValues.add(keyString);

 				jobDone = storeInMemcached(groupKey, group);
 			}
 		}
    }

    /**
     * Stores an object identified by a key in Memcached.
     *
     * @param keyString the object key
     * @param value the object has to be stored.
     */
    private void storeInMemcached(String keyString, Object value) {
        if (value != null
                && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new CacheException("Object of type '"
                    + value.getClass().getName()
                    + "' that's non-serializable is not supported by Memcached");
        }
        client.set(keyString, value, new Date(configuration.getExpiration() * 1000));
    }

	/**
	 * Tries to update an object value in memcached considering the cas validation
	 * 
	 * Returns true if the object passed the cas validation and was modified.
	 * 
	 * @param keyString
	 * @param value
	 * @return
	 */
	private boolean storeInMemcached(String keyString, ObjectWithCas value) {
		if (value != null && value.getObject() != null && !Serializable.class.isAssignableFrom(value.getObject().getClass())) {
			throw new CacheException("Object of type '" + value.getObject().getClass().getName() + "' that's non-serializable is not supported by Memcached");
		}
		
		return client.cas(keyString, value.getObject(), value.getCas());
	}

	/**
	 * Tries to store an object identified by a key in Memcached.
	 * 
	 * Will fail if the object already exists.
	 * 
	 * @param keyString
	 * @param value
	 * @return
	 */
	private boolean tryToAdd(String keyString, Object value) {
		if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
			throw new CacheException("Object of type '" + value.getClass().getName() + "' that's non-serializable is not supported by Memcached");
		}
		return client.add(keyString, value, new Date(configuration.getExpiration() * 1000));
	}

    public Object removeObject(Object key) {
        String keyString = toKeyString(key);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing object '"
                    + keyString
                    + "'");
        }

        Object result = getObject(key);
        if (result != null) {
            client.delete(keyString);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
	public void removeGroup(String id) {
		String groupKeyString = toKeyString(id);
        ObjectWithCas group = null;
        boolean result = false;
        do {
            group = getGroup(groupKeyString);
            if (group == null || group.getObject() == null) {
                return;
            }
            
            Set<String> groupValues = (Set<String>) group.getObject();
            for (String key : groupValues) {
                client.delete(key);
            }
            
            result = client.delete(groupKeyString);
        } while(!result);
        
	}

    @Override
    protected void finalize() throws Throwable {
        SockIOPool.getInstance().shutDown();
        super.finalize();
    }

}
