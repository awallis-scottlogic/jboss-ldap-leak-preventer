# jboss-ldap-leak-preventer
A JBoss extension implementing a workaround for the JNDI/LDAP memory leak.

## Overview
The JNDI/LDAP implementation that ships with Oracle's JVM has the potential to cause a classloader memory leak. Tomcat's [JreMemoryLeakPreventionListener](https://tomcat.apache.org/tomcat-7.0-doc/config/listeners.html#JRE_Memory_Leak_Prevention_Listener_-_org.apache.catalina.core.JreMemoryLeakPreventionListener) and Jetty's [LDAPLeakPreventer](http://www.eclipse.org/jetty/documentation/current/preventing-memory-leaks.html#preventers-table) both provide workarounds. JBoss does not appear to provide a workaround out-the-box, this JBoss extension provides one.

## Usage
Build and package the extension using `mvn package`. This command will create a JBoss module for the extension under target/module. Copy the module to your JBoss installation by copying the `com` folder from under `target/module` to `<JBOSS-HOME>/modules` - merging with any existing modules that share the `com` package prefix.

Edit the JBoss `standalone.xml` configuration file as follows:
  1. Add `<extension module="com.scottlogic.ldapleakpreventer"/>` to the `extensions` element.
  1. Add <subsystem xmlns="urn:scottlogic:ldapleakpreventer:1.0"/> to the `profile` element.
  1. Add the LDAP-specific properties to the `system-properties` element.
     These properties can be specified at the command line or as environment variables, but must be present at the time the extension is loaded.
