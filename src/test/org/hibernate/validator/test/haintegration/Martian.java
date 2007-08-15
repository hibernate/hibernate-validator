//$Id: Martian.java 7797 2005-08-10 10:40:48Z epbernard $
package org.hibernate.validator.test.haintegration;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Martian {
	private MartianPk id;
	private MarsAddress address;

	@Id
	public MartianPk getId() {
		return id;
	}

	public void setId(MartianPk id) {
		this.id = id;
	}

	public MarsAddress getAddress() {
		return address;
	}

	public void setAddress(MarsAddress address) {
		this.address = address;
	}

}
