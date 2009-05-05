package org.hibernate.validation.engine.graphnavigation;

import javax.validation.constraints.NotNull;
import javax.validation.Valid;

/**
 * @author Emmanuel Bernard
 */
public class Parent {
	private String name;
	private Child child;

	@NotNull(groups = ParentSecond.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Valid
	public Child getChild() {
		return child;
	}

	public void setChild(Child child) {
		this.child = child;
	}
}
