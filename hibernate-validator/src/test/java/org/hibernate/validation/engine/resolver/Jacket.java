package org.hibernate.validation.engine.resolver;

import javax.validation.constraints.Max;


/**
 * @author Emmanuel Bernard
 */
public class Jacket {
	Integer width;

	@Max(30)
	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}
}
