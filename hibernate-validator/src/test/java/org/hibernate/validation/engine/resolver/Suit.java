package org.hibernate.validation.engine.resolver;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.Valid;
import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author Emmanuel Bernard
 */
@GroupSequence( {Suit.class, Cloth.class })
public class Suit {
	@Max(value=50, groups = { Default.class, Cloth.class})
	@Min(1)
	private Integer size;
	@Valid private Trousers trousers;
	private Jacket jacket;

	public Trousers getTrousers() {
		return trousers;
	}

	public void setTrousers(Trousers trousers) {
		this.trousers = trousers;
	}

	@Valid
	public Jacket getJacket() {
		return jacket;
	}

	public void setJacket(Jacket jacket) {
		this.jacket = jacket;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}
