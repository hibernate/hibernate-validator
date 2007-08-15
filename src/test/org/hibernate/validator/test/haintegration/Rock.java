//$Id: Rock.java 9795 2006-04-26 06:41:18Z epbernard $
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
