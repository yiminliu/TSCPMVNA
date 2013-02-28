package com.tscp.mvne.network.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.internal.ws.client.BindingProviderProperties;
import com.tscp.mvne.config.CONFIG;
import com.tscp.mvne.config.CONNECTION;
import com.tscp.mvne.exception.InitializationException;
import com.tscp.mvno.webservices.API3;
import com.tscp.mvno.webservices.API3Service;

/**
 * Instantiates and provides access to a singleton of API3Service.
 * 
 * @author Tachikoma
 * 
 */
public final class NetworkGatewayProvider {
	private static final Logger logger = LoggerFactory.getLogger("TSCPMVNA");
	private static final API3Service service = loadInterface();
	private static final API3 port = service.getAPI3Port();

	protected NetworkGatewayProvider() {
		// prevent instantiation
	}

	/**
	 * Loads and returns the API3 Service.
	 * 
	 * @return
	 */
	protected static final API3Service loadInterface() throws InitializationException {
		CONFIG.initAll();
		try {
			API3Service service = new API3Service(new URL(CONNECTION.networkWSDL), new QName(CONNECTION.networkNameSpace, CONNECTION.networkServiceName));
			logger.info("Service initialized to " + service.getWSDLDocumentLocation().toString());
			return service;
		} catch (MalformedURLException url_ex) {
			logger.error("Exception initializing service at " + CONNECTION.networkWSDL, url_ex);
			throw new InitializationException(url_ex);
		}
	}

	/**
	 * Returns the singleton instance of the API3 Service.
	 * 
	 * @return
	 */
	public static final API3 getInstance() {

		Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
		requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 180000);
		requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 180000);

		return port;
	}
}
