package com.chickling.kmanager.jmx;

import javax.management.remote.JMXConnector;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public interface JMXExecutor {

	public void doWithConnection(JMXConnector jmxc);

}
