/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.lang.reflect.Executable;
import java.util.Optional;

import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;

/**
 * Extension of {@link BaseBeanValidationContext} for executable validation.
 *
 * @author Marko Bekhta
 */
public interface ExecutableValidationContext<T> extends BaseBeanValidationContext<T> {

	Executable getExecutable();

	Optional<ExecutableMetaData> getExecutableMetaData();

}
