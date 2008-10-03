//$Id$
package org.hibernate.validator.test.jpa;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.validator.Length;
import org.hibernate.validator.Min;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Cat implements Serializable {
	private Integer id;
	private String name;
	private long length;

	@Id
	@GeneratedValue
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Length(min = 4)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Min(0)
	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}
}