/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.list;

//end::include[]
import java.util.ArrayList;
import java.util.List;

//tag::include[]
public class Car {

	private List<@ValidPart String> parts = new ArrayList<>();

	public void addPart(String part) {
		parts.add( part );
	}

	//...

}
//end::include[]
