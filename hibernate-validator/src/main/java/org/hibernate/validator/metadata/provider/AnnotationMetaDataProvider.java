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
package org.hibernate.validator.metadata.provider;

import java.util.Set;

import org.hibernate.validator.metadata.AnnotationIgnores;
import org.hibernate.validator.metadata.BeanConfiguration;

/**
 * @author Gunnar Morling
 */
public class AnnotationMetaDataProvider implements MetaDataProvider {

	/* (non-Javadoc)
	 * @see org.hibernate.validator.metadata.provider.MetaDataProvider#getAllBeanConfigurations()
	 */
	public Set<BeanConfiguration<?>> getAllBeanConfigurations() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.hibernate.validator.metadata.provider.MetaDataProvider#getAnnotationIgnores()
	 */
	public AnnotationIgnores getAnnotationIgnores() {
		return new AnnotationIgnores();
	}

}
