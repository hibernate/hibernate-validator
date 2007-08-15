//$Id$
package org.hibernate.validator.test.inheritance;

import java.io.Serializable;

import org.hibernate.validator.Length;

/**
 * @author Emmanuel Bernard
 */
public class Dog extends Animal implements Serializable, BoneEater {
	private String favoriteBone;
	private int frequency;

	@Length(min = 3)
	public String getFavoriteBone() {
		return favoriteBone;
	}

	public void setFavoriteBone(String favoriteBone) {
		this.favoriteBone = favoriteBone;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
}
