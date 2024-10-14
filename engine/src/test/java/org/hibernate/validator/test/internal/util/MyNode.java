/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util;

/**
 * @author Hardy Ferentschik
 */
public class MyNode extends Node<Integer> {
	public MyNode(Integer data) {
		super( data );
	}

	@Override
	public void setData(Integer data) {
		super.setData( data );
	}
}
