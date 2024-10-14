/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

/**
 * @author Hardy Ferentschik
 */
public class System {
	private String name;

	private List<Part> parts;

	public System() {
		parts = new ArrayList<Part>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Valid
	public List<Part> getParts() {
		return parts;
	}

	public void addPart(Part part) {
		parts.add( part );
	}
}
