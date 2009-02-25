package org.hibernate.validation.engine.groups;

import javax.validation.GroupSequence;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import org.hibernate.validation.constraints.Length;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
@GroupSequence({ Car.class, Car.Test.class })
public class Car {
	@Pattern(regexp = ".*", groups = Default.class)
	@Length(min = 2, max = 20, groups = Car.Test.class)
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public interface Test {

	}
}