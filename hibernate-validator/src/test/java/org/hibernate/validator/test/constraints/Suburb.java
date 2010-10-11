/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.constraints;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public class Suburb {
	public enum Facility {
		SHOPPING_MALL, BUS_TERMINAL
	}

	@Size(min = 5, max = 10, message = "size must be between {min} and {max}")
	private String name;

	@Size(min = 2, max = 2, message = "size must be between {min} and {max}")
	private Map<Facility, Boolean> facilities;

	@Size(min = 2, message = "size must be between {min} and {max}")
	private Set<String> streetNames;

	@Size(min = 4, max = 1000, message = "size must be between {min} and {max}")
	private Coordinate[] boundingBox;

	@PostCodeList
	private Collection<? extends Number> includedPostCodes;

	public void setIncludedPostCodes(Collection<? extends Number> includedPostCodes) {
		this.includedPostCodes = includedPostCodes;
	}

	public Collection<? extends Number> getIncludedPostcodes() {
		return includedPostCodes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Map<Facility, Boolean> getFacilities() {
		return facilities;
	}

	public void addFacility(Facility f, Boolean exist) {
		if ( facilities == null ) {
			facilities = new HashMap<Facility, Boolean>();
		}
		facilities.put( f, exist );
	}

	public Set<String> getStreetNames() {
		return streetNames;
	}

	public void addStreetName(String streetName) {
		if ( streetNames == null ) {
			streetNames = new HashSet<String>();
		}
		streetNames.add( streetName );
	}

	public Coordinate[] getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(Coordinate[] boundingBox) {
		this.boundingBox = boundingBox;
	}
}

