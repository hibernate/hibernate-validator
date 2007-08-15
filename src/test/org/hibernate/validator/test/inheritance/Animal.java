//$Id$
package org.hibernate.validator.test.inheritance;

import java.io.Serializable;

/**
 * @author Emmanuel Bernard
 */
public class Animal implements Serializable, Name {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
