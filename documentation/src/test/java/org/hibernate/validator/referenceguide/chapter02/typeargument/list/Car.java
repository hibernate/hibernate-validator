//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.typeargument.list;

//end::include[]

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

//tag::include[]
public class Car {

	@Valid
	private List<@ValidPart String> parts = new ArrayList<>();

	public void addPart(String part) {
		parts.add( part );
	}

	//...

}
//end::include[]
