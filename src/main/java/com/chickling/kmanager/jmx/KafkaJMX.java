package com.chickling.kmanager.jmx;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class KafkaJMX {
	private static Logger LOG = LoggerFactory.getLogger(KafkaJMX.class);

	private Map<String, Object> defaultJmxConnectorProperties = new HashMap<String, Object>();

	public KafkaJMX() {
		initDefaultJMXConnectorProperties();
	}

	private void initDefaultJMXConnectorProperties() {
		defaultJmxConnectorProperties.put("jmx.remote.x.request.waiting.timeout", "3000");
		defaultJmxConnectorProperties.put("jmx.remote.x.notification.fetch.timeout", "3000");
		defaultJmxConnectorProperties.put("sun.rmi.transport.connectionTimeout", "3000");
		defaultJmxConnectorProperties.put("sun.rmi.transport.tcp.handshakeTimeout", "3000");
		defaultJmxConnectorProperties.put("sun.rmi.transport.tcp.responseTimeout", "3000");
	}

	public void doWithConnection(String jmxHost, int jmxPort, Optional<String> jmxUser, Optional<String> jmxPasswd,
			boolean jmxSSL, JMXExecutor excutor) {
		String urlStr = "service:jmx:rmi:///jndi/rmi://" + jmxHost + ":" + jmxPort + "/jmxrmi";
		JMXConnector jmxc = null;
		try {
			JMXServiceURL url = new JMXServiceURL(urlStr);
			// authenticate
			Map<String, Object> env = new HashMap<String, Object>();
			String[] credentials = { jmxUser.orElse(""), jmxPasswd.orElse("") };
			env.put(JMXConnector.CREDENTIALS, credentials);

			if (jmxSSL) { // com.sun.management.jmxremote.registry.ssl=true
				env.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());
			}
			jmxc = JMXConnectorFactory.connect(url, env);
			excutor.doWithConnection(jmxc);
		} catch (Exception e) {
			LOG.error("KafkaJMX doWithConnection error! " + e.getMessage());
		}
	}

}
