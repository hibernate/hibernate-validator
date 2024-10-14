/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;

import jakarta.validation.Valid;

public class ValidationUsingAtValidAnnotation {

	@Valid
	public Integer integer;

	@Valid
	public Collection<?> collection1;

	/**
	 * Not allowed (primitive type).
	 */
	@Valid
	public int primitiveInt;

	/**
	 * Not allowed (static field).
	 */
	@Valid
	public static Integer staticInteger;

	@Valid
	public Integer getInteger() {
		return null;
	}

	@Valid
	public Collection<?> getCollection() {
		return null;
	}

	/**
	 * Not allowed (primitive type).
	 */
	@Valid
	public int getPrimitiveInt() {
		return 0;
	}

	/**
	 * Not allowed (static field).
	 */
	@Valid
	public static Integer getStaticInteger() {
		return null;
	}

	/**
	 * Not allowed (no getter).
	 */
	@Valid
	public Collection<?> getCollectionWithParams(int i) {
		return null;
	}

	/**
	 * Not allowed (no getter).
	 */
	@Valid
	public Collection<?> setCollection() {
		return null;
	}

	/**
	 * Not allowed (no getter).
	 */
	@Valid
	public void getVoid() {
	}
}
