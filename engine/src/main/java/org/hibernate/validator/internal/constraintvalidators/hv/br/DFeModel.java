/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

/**
 * @author Matheus Pereira
 */
enum DFeModel {

	NFE( "55" ),
	CTE( "57" ),
	MDFE( "58" ),
	NFCE( "65" ),
	CTE_OS( "67" );

	private final String code;

	DFeModel(String code) {
		this.code = code;
	}

	String getCode() {
		return code;
	}
}
