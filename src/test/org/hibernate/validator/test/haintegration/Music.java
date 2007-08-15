//$Id: Music.java 9795 2006-04-26 06:41:18Z epbernard $
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
