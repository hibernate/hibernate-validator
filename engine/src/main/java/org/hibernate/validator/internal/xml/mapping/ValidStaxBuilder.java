/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.xml.mapping;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * @author Marko Bekhta
 */
class ValidStaxBuilder extends AbstractStaxBuilder {

	private static final String VALID_QNAME_LOCAL_PART = "valid";
	private Boolean cascading;

	@Override
	protected String getAcceptableQName() {
		return VALID_QNAME_LOCAL_PART;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) {
		cascading = true;
	}

	public boolean build() {
		return cascading == null ? false : true;
	}
}
