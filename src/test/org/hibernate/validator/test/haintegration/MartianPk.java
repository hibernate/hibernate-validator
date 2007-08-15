//$Id: MartianPk.java 8593 2005-11-17 18:12:11Z epbernard $
package org.hibernate.validator.test.haintegration;

import java.io.Serializable;
import javax.persistence.Embeddable;

import org.hibernate.validator.Length;

/**
 * @author Emmanuel Bernard
 */
@Embeddable
public class MartianPk implements Serializable {
	private String name;
	private String colony;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Length(max = 4)
	public String getColony() {
		return colony;
	}

	public void setColony(String colony) {
		this.colony = colony;
	}

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;

		final MartianPk martianPk = (MartianPk) o;

		if ( !colony.equals( martianPk.colony ) ) return false;
		if ( !name.equals( martianPk.name ) ) return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = name.hashCode();
		result = 29 * result + colony.hashCode();
		return result;
	}
}
