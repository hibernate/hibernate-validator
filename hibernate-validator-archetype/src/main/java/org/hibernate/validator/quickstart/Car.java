/**
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */	
package org.hibernate.validator.quickstart;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An exemplary model class, which is enriched with constraint annotations from
 * the Bean Validation API (<a href="http://jcp.org/en/jsr/detail?id=303">JSR
 * 303</a>). Have a look at {@link CarTest} to learn, how the Bean Validation
 * API can be used to validate Car instances.
 * 
 * @author Gunnar Morling
 */
public class Car {

	/**
	 * By annotating the field with @NotNull we specify, that null is not a valid
	 * value.
	 */
    @NotNull
    private String manufacturer;

    /**
     * This String field shall not only not allowed to be null, it shall also between
     * 2 and 14 characters long. 
     */
    @NotNull
    @Size(min = 2, max = 14)
    private String licensePlate;

    /**
     * This int field shall have a value of at least 2.
     */
    @Min(2)
    private int seatCount;
    
    public Car(String manufacturer, String licencePlate, int seatCount) {

        this.manufacturer = manufacturer;
        this.licensePlate = licencePlate;
        this.seatCount = seatCount;
    }

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public int getSeatCount() {
		return seatCount;
	}

	public void setSeatCount(int seatCount) {
		this.seatCount = seatCount;
	}

}