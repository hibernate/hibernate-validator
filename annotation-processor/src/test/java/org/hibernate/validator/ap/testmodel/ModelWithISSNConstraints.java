package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.ISSN;

/**
 * @author Andrea Boriero
 */
public class ModelWithISSNConstraints {

	@ISSN
	private String string;

	@ISSN
	private CharSequence charSequence;

	@ISSN
	private Integer integer;

}
