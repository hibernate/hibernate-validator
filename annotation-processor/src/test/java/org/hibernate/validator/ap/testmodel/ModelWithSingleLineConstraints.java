package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.SingleLine;

public class ModelWithSingleLineConstraints {

	@SingleLine
	public Collection<String> collection;

	@SingleLine
	public List<String> list;

	@SingleLine
	public Set<String> set;

	@SingleLine
	public String string;
}
