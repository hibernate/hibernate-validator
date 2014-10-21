/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

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


