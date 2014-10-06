/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.valuehandling;

/**
 * Determines how unwrapping of values should occur.
 *
 * @author Hardy Ferentschik
 * @see org.hibernate.validator.valuehandling.UnwrapValidatedValue
 * @see org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper
 */
public enum UnwrapMode {
	/**
	 * Automatic occurs if there is no explicit {@code UnwrapValidatedValue} annotation. Whether unwrapping occurs depends
	 * on the number and type of the registered {@code ValidatedValueUnwrapper} instances and the available constraint
	 * validator instances for the wrapper as well as wrapped value.
	 */
	AUTOMATIC,
	/**
	 * Unwrapping of the value is explicitly configured, eg via {@code UnwrapValidatedValue}
	 */
	UNWRAP,
	/**
	 * Unwrapping the value is explicitly prohibited, eg via {@code UnwrapValidatedValue(false)}
	 */
	SKIP_UNWRAP
}
