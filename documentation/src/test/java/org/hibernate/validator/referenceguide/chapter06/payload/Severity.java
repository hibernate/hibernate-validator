/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.payload;

import jakarta.validation.Payload;

//tag::include[]
public class Severity {
	public interface Info extends Payload {
	}

	public interface Error extends Payload {
	}
}
//end::include[]
