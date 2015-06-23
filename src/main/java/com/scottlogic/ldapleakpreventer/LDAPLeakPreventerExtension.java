package com.scottlogic.ldapleakpreventer;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.logging.Logger;

/**
 * JBoss extension that loads the <code>com.sun.jndi.ldap.LdapPoolManager</code> class up front to prevent a
 * web-application classloader leak that would otherwise occur if the web-application itself was responsible for
 * triggering the LdapPoolManager class to be loaded.
 */
public class LDAPLeakPreventerExtension implements Extension {
    private static final Logger log = Logger.getLogger(LDAPLeakPreventerExtension.class.getPackage().getName());

    private static final String POOL_TIMEOUT_PROPERTY = "com.sun.jndi.ldap.connect.pool.timeout";
    private static final int MAX_POLL_COUNT = 20;
    private static final long ONE_SECOND = 1000;
    private static final String LDAP_POOL_MANAGER_CLASS = "com.sun.jndi.ldap.LdapPoolManager";

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        // no parsers required
    }

    /**
     * Checks if the <code>com.sun.jndi.ldap.connect.pool.timeout</code> is defined, and if not starts a polling thread
     * that polls the property for 20 seconds. If the property us configured within this timeout, the LdapPoolManager
     * class is loaded.
     *
     * This polling mechanism is a workaround for the fact that the JBoss standalone.xml configuration's
     * <code>system-properties</code> element is not processed until after the extensions have been instantiated and
     * initialized. If we load the LdapPoolManager class before the timeout property exists, the LdapPoolManager class
     * will use a default value of 0 instead of the value configured. In the case where the timeout property is
     * intentionally omitted from any configuration, this implementation will avoid loading the LdapPoolManager class.
     */
    @Override
    public void initialize(ExtensionContext context) {
        String timeoutProperty = System.getProperty(POOL_TIMEOUT_PROPERTY);
        if (timeoutProperty != null) {
            log.info("initialize: pool timeout is configured to " + timeoutProperty + " ms");
            loadLdapPoolManagerClass();
        } else {
            log.info("initialize: pool timeout property has not yet been set, starting polling thread");
            Thread pollerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < MAX_POLL_COUNT; i++) {
                            Thread.sleep(ONE_SECOND);
                            String property = System.getProperty(POOL_TIMEOUT_PROPERTY);
                            if (property != null) {
                                log.info("pool timeout property is configured to " + property + " ms");
                                loadLdapPoolManagerClass();
                                return;
                            }
                        }
                    } catch (InterruptedException e) {
                        log.info("polling thread interrupted early");
                    }
                    log.info("pool timeout property not configured within timeout, not loading LdapPoolManager.");
                }
            }, "LDAPLeakPreventerExtension-PropertyPoller");
            pollerThread.setDaemon(true);
            pollerThread.start();
        }
    }

    private static void loadLdapPoolManagerClass() {
        try {
            Class.forName(LDAP_POOL_MANAGER_CLASS);
            log.info("Loaded LdapPoolManager class");
        } catch (ClassNotFoundException e) {
            log.info("Error loading LdapPoolManager class", e);
        }
    }
}
