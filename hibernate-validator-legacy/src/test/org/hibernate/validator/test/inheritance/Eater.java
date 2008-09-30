//$Id$
package org.hibernate.validator.test.inheritance;

import org.hibernate.validator.Min;

/**
 * @author Emmanuel Bernard
 */
public interface Eater {
	@Min(2)
	int getFrequency();

	void setFrequency(int frequency);
}
