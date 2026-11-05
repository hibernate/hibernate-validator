package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.kor.KorRRN;

/**
 * @author Marko Bekhta
 */
public class ModelWithKorRRNConstraints {

	@KorRRN
	private String string;

	@KorRRN
	private CharSequence charSequence;

	@KorRRN
	private Integer integer;

}
