package org.hibernate.validator.test.haintegration;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Music {
	@Id
	public String name;
}
