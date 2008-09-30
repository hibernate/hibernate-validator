//$Id$
package org.hibernate.validator.test.inheritance;

import org.hibernate.validator.NotNull;

/**
 * @author Emmanuel Bernard
 */
public interface BoneEater extends Eater {
	@NotNull
	String getFavoriteBone();

	void setFavoriteBone(String favoriteBone);
}
