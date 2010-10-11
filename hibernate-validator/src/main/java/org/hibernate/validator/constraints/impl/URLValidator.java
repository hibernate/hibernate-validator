/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.constraints.impl;

import java.net.MalformedURLException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.URL;

/**
 * Validate that the string is a valid URL.
 *
 * @author Hardy Ferentschik
 */
public class URLValidator implements ConstraintValidator<URL, String> {
	private String protocol;
	private String host;
	private int port;

	public void initialize(URL url) {
		this.protocol = url.protocol();
		this.host = url.host();
		this.port = url.port();
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}

		java.net.URL url;
		try {
			url = new java.net.URL( value );
		}
		catch ( MalformedURLException e ) {
			return false;
		}

		if ( protocol != null && protocol.length() > 0 && !url.getProtocol().equals( protocol ) ) {
			return false;
		}

		if ( host != null && host.length() > 0 && !url.getHost().equals( host ) ) {
			return false;
		}

		if ( port != -1 && url.getPort() != port ) {
			return false;
		}

		return true;
	}
}
