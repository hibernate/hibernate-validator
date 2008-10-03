//$Id$
package org.hibernate.validator.test.inheritance;

import org.hibernate.validator.NotNull;

/**
 * @author Emmanuel Bernard
 */
public interface Name {
	@NotNull
	String getName();

	void setName(String name);
}
