//$Id: $
package org.hibernate.validator.test.haintegration;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class CreditCard {
	@Id
	@GeneratedValue
	private Integer id;
	@Embedded
	@Valid
	@NotNull
	private User username;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUsername() {
		return username;
	}

	public void setUsername(User username) {
		this.username = username;
	}

}
