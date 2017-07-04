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
