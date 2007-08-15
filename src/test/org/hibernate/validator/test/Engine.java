//$Id: $
package org.hibernate.validator.test;

import org.hibernate.validator.Patterns;
import org.hibernate.validator.Pattern;

/**
 * @author Emmanuel Bernard
 */
public class Engine {
	@Patterns( {
			@Pattern(regex = "^[A-Z0-9-]+$", message = "must contain alphabetical characters only"),
			@Pattern(regex = "^....-....-....$", message="must match ....-....-....")
			} )
	private String serialNumber;
	private long horsePower;


	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public long getHorsePower() {
		return horsePower;
	}

	public void setHorsePower(long horsePower) {
		this.horsePower = horsePower;
	}
}
