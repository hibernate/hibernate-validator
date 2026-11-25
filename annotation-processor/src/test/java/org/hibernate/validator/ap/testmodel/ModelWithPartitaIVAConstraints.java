package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.it.PartitaIVA;

public class ModelWithPartitaIVAConstraints {

	@PartitaIVA
	public String string;

	@PartitaIVA
	public CharSequence charSequence;

	@PartitaIVA
	public Integer integer;
}
