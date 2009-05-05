package org.hibernate.validation.engine.graphnavigation;

import javax.validation.constraints.NotNull;

/**
 * @author Emmanuel Bernard
 */
public class Child {
	private String name;

	@NotNull(groups = ChildFirst.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
