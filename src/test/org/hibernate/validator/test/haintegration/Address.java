//$Id: Address.java 9755 2006-04-17 19:44:22Z epbernard $
package org.hibernate.validator.test.haintegration;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;
import org.hibernate.validator.Range;

/**
 * @author Gavin King
 */
@Entity
public class Address {
	@NotNull
	public static String blacklistedZipCode;

	private String line1;
	private String line2;
	private String zip;
	private String state;
	@Length(max = 20)
	@NotNull
	private String country;
	private long id;
	private boolean internalValid = true;
	@Range(min = -2, max = 50, message = "{floor.out.of.range} (escaping #{el})")
	public int floor;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@NotNull
	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	@Length(max = 3)
	@NotNull
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Length(max = 5, message = "{long}")
	@Pattern(regex = "[0-9]+")
	@NotNull
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@AssertTrue
	@Transient
	public boolean isValid() {
		return true;
	}

	@AssertTrue
	@Transient
	private boolean isInternalValid() {
		return internalValid;
	}

	public void setInternalValid(boolean internalValid) {
		this.internalValid = internalValid;
	}

	@Id
	@Min(1)
	@Range(max = 2000)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
