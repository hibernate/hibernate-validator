//$Id: $
package org.hibernate.validator.test.valid;

import org.hibernate.validator.NotNull;

/**
 * @author Emmanuel Bernard
 */
public class Address {

	private String city;

	@NotNull
	public String getCity() {
		return city;
	}
} 
