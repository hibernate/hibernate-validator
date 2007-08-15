//$Id$
package org.hibernate.validator.test;

import java.math.BigInteger;
import java.util.Date;

import org.hibernate.validator.Future;
import org.hibernate.validator.Length;
import org.hibernate.validator.Min;

/**
 * @author Emmanuel Bernard
 */
public class Tv {
	@Length(max = 2)
	public String serial;
	public int size;
	@Length(max = 2)
	public String name;
	@Future
	public Date expDate;
	@Length(min = 0)
	public String description;
	@Min(1000)
	public BigInteger lifetime;
}
