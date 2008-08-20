//$Id$
package org.hibernate.validator.test.haintegration;

import javax.persistence.Entity;

import org.hibernate.validator.NotNull;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Rock extends Music {
	@NotNull
	public Integer bit;
}
