//$Id: VenusianPk.java 7797 2005-08-10 10:40:48Z epbernard $
package org.hibernate.validator.test.haintegration;

import java.io.Serializable;

/**
 * @author Emmanuel Bernard
 */
public class VenusianPk implements Serializable {
	private String region;
	private String name;

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;

		final VenusianPk that = (VenusianPk) o;

		if ( !name.equals( that.name ) ) return false;
		if ( !region.equals( that.region ) ) return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = region.hashCode();
		result = 29 * result + name.hashCode();
		return result;
	}
}
