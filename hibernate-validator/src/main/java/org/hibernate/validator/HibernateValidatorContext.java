/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.hibernate.validator;

import javax.validation.ValidatorContext;

/**
 * Provide Hibernate Validator context specific configuration and
 * standard Bean Validation configuration.
 *
 * @author Emmanuel Bernard
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public interface HibernateValidatorContext extends ValidatorContext {
	/**
	 * Enable or disable fail fast. When fail fast is enabled the validation
	 * will stop on the first constraint violation detected.
	 *
	 * @param failFast true to enable failFast
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	HibernateValidatorContext failFast(boolean failFast);
}
