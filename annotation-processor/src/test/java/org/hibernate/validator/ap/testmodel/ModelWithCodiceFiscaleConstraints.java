package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.it.CodiceFiscale;

public class ModelWithCodiceFiscaleConstraints {

	@CodiceFiscale
	public String string;

	@CodiceFiscale
	public CharSequence charSequence;

	@CodiceFiscale
	public Integer integer;
}
