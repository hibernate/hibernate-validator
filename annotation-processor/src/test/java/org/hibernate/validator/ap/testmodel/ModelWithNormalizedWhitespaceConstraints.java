package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.NormalizedWhitespace;

public class ModelWithNormalizedWhitespaceConstraints {

	@NormalizedWhitespace
	public Collection<String> collection;

	@NormalizedWhitespace
	public List<String> list;

	@NormalizedWhitespace
	public Set<String> set;

	@NormalizedWhitespace
	public String string;
}
