/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter09;

//tag::include[]
/**
 * Application-provided service for checking whether a password was used
 * previously by the current user. The implementation is responsible for
 * resolving the current user identity (e.g. from a request-scoped context),
 * hashing the candidate password, and comparing it against stored hashes.
 */
public interface PasswordHistoryService {

	boolean isPreviouslyUsed(char[] password);
}
//end::include[]
