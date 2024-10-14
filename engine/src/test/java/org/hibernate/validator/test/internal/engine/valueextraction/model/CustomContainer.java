/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import java.util.Iterator;

/**
 * @author Marko Bekhta
 */
public interface CustomContainer<T> {

	Iterator<T> iterator();
}
