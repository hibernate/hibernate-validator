package org.hibernate.validator.integration.cdi.service;

/**
 * @author Hardy Ferentschik
 */
public class PingServiceImpl implements PingService {
	@Override
	public String ping() {
		return "pong";
	}
}
