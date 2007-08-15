//$Id: Venusian.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator.test.haintegration;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.hibernate.validator.Length;

/**
 * @author Emmanuel Bernard
 */
@Entity
@IdClass(VenusianPk.class)
public class Venusian implements Serializable {
	private String region;
	private String name;

	@Id
	@Length(min = 3)
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	@Id
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
