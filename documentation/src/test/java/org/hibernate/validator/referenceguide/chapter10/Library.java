//tag::include[]
package org.hibernate.validator.referenceguide.chapter10;

//end::include[]

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@SuppressWarnings("unused")
//tag::include[]
public class Library {

	@NotNull
	private String name;

	private List<@NotNull @Valid Book> books;

	//getters and setters ...
}
//end::include[]
