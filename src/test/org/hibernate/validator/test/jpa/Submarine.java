//$Id: $
package org.hibernate.validator.test.jpa;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.hibernate.validator.event.JPAValidateListener;

/**
 * @author Emmanuel Bernard
 */
@Entity
@EntityListeners( JPAValidateListener.class )
public class Submarine {
	@Id
	@GeneratedValue
	private Long id;

	@NotEmpty
	private String name;

	@Valid
	private Commander commander;

	@Min( 10 )
	private long size;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Commander getCommander() {
		return commander;
	}

	public void setCommander(Commander commander) {
		this.commander = commander;
	}
}
