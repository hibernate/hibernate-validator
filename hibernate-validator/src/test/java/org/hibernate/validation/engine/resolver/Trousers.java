package org.hibernate.validation.engine.resolver;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.groups.Default;

/**
 * @author Emmanuel Bernard
 */
public class Trousers {
	@Min(value=70, groups = {Default.class, Cloth.class})
	@Max(value=220)
	private Integer length;

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}
}
