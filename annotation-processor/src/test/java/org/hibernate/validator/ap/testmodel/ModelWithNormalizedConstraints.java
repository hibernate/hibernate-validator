package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Normalized;

public class ModelWithNormalizedConstraints {

	@Normalized
	public Collection<String> collection;

	@Normalized
	public List<String> list;

	@Normalized
	public Set<String> set;

	@Normalized
	public String string;
}
