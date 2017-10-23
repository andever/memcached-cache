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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Converter from the Config to a proper {@link MemcachedConfiguration}.
 *
 * @author Simone Tripodi
 */
final class MemcachedConfigurationBuilder {

    /**
     * This class instance.
     */
    private static final MemcachedConfigurationBuilder INSTANCE = new MemcachedConfigurationBuilder();

    private static final String SYSTEM_PROPERTY_MEMCACHED_PROPERTIES_FILENAME = "memcached.properties.filename";

    /**
     *
     */
    private static final String MEMCACHED_RESOURCE = "memcached.properties";

    private final String memcachedPropertiesFilename;

    /**
     * The setters used to extract properties.
     */
    private final List<AbstractPropertySetter<?>> settersRegistry = new ArrayList<AbstractPropertySetter<?>>();

    /**
     * Hidden constructor, this class can't be instantiated.
     */
    private MemcachedConfigurationBuilder() {
        memcachedPropertiesFilename = System.getProperty(SYSTEM_PROPERTY_MEMCACHED_PROPERTIES_FILENAME, MEMCACHED_RESOURCE);

        settersRegistry.add(new StringPropertySetter("org.mybatis.caches.memcached.keyprefix", "keyPrefix", "_mybatis_"));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.expiration", "expiration", 0));
        settersRegistry.add(new StringArrayPropertySetter("org.mybatis.caches.memcached.servers", "servers", new String[]{"localhost:11211"}));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.initconn", "initConn", 10));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.minConn", "minConn", 5));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.maxConn", "maxConn", 100));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.maxIdle", "maxIdle", 1000 * 60 * 5));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.maxbusytime", "maxBusyTime", 1000 * 30));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.socketto", "socketTO", 1000 * 3));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.socketconnectto", "socketConnectTO", 1000 * 3));
        settersRegistry.add(new BooleanPropertySetter("org.mybatis.caches.memcached.failover", "failover", true));
        settersRegistry.add(new BooleanPropertySetter("org.mybatis.caches.memcached.failback", "failback", true));
        settersRegistry.add(new BooleanPropertySetter("org.mybatis.caches.memcached.nagle", "nagle", false));
        settersRegistry.add(new BooleanPropertySetter("org.mybatis.caches.memcached.alivecheck", "aliveCheck", false));

    }

    /**
     * Return this class instance.
     *
     * @return this class instance.
     */
    public static MemcachedConfigurationBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * Parses the Config and builds a new {@link MemcachedConfiguration}.
     *
     * @return the converted {@link MemcachedConfiguration}.
     */
    public MemcachedConfiguration parseConfiguration() {
        return parseConfiguration(getClass().getClassLoader());
    }

    /**
     * Parses the Config and builds a new {@link MemcachedConfiguration}.
     *
     * @param the {@link ClassLoader} used to load the {@code memcached.properties} file in classpath.
     * @return the converted {@link MemcachedConfiguration}.
     */
    public MemcachedConfiguration parseConfiguration(ClassLoader classLoader) {
        Properties config = new Properties();

        // load the properties specified from /memcached.properties, if present
        InputStream input = classLoader.getResourceAsStream(memcachedPropertiesFilename);
        if (input != null) {
            try {
                config.load(input);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading classpath property '"
                        + memcachedPropertiesFilename
                        + "', see nested exceptions", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    // close quietly
                }
            }
        }

        MemcachedConfiguration memcachedConfiguration = new MemcachedConfiguration();

        for (AbstractPropertySetter<?> setter : settersRegistry) {
            setter.set(config, memcachedConfiguration);
        }

        return memcachedConfiguration;
    }

}
