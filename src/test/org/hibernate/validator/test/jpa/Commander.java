//$Id: $
package org.hibernate.validator.test.jpa;

import javax.persistence.Embeddable;
import javax.persistence.Column;

import org.hibernate.validator.NotEmpty;

/**
 * @author Emmanuel Bernard
 */
@Embeddable
public class Commander {
	@NotEmpty
	@Column(name="commander_name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
