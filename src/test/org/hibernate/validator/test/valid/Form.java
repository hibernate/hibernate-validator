//$Id: $
package org.hibernate.validator.test.valid;

import org.hibernate.validator.Valid;

/**
 * @author Emmanuel Bernard
 */
public class Form {

	private Member member;

	@Valid
	public Member getMember() {
		return member;
	}

	public void setMember(Member m) {
		this.member = m;
	}
}
