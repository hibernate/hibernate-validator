/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the meta-data of a Java method's return value relevant in the
 * context of bean validation, for instance the constraints at this return value
 * and whether cascading validation shall be applied to it or not.
 * 
 * @author Gunnar Morling
 * 
 */
public class ReturnValueMetaData implements Iterable<MetaConstraint<?, ? extends Annotation>> {

	private final List<MetaConstraint<?, ? extends Annotation>> constraints;

	private final boolean isCascading;
	
	public ReturnValueMetaData(List<MetaConstraint<?, ? extends Annotation>> constraints, boolean isCascading) {
		
		this.constraints = constraints;
		this.isCascading = isCascading;
	}

	public boolean isCascading() {
		return isCascading;
	}

	public Iterator<MetaConstraint<?, ? extends Annotation>> iterator() {
		return constraints.iterator();
	}
		
}