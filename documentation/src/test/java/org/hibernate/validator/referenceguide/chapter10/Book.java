//tag::include[]
package org.hibernate.validator.referenceguide.chapter10;

//end::include[]

import javax.validation.constraints.NotEmpty;

//tag::include[]
public class Book {

	@NotEmpty
	private String title;

	@NotEmpty
	private String author;

	//getters and setters ...
}
//end::include[]
