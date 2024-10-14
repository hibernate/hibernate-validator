/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution;

import java.io.Serializable;
import java.util.Collection;

public interface SerializableCollection<T> extends Serializable, Collection<T> {

}
