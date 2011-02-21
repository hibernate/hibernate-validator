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
package org.hibernate.validator.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;

/**
 * An aggregated view of the constraint related meta data for a given method and
 * all the methods in the inheritance hierarchy which it overrides or
 * implements.
 *
 * @author Gunnar Morling
 */
public class AggregatedMethodMetaData implements Iterable<BeanMetaConstraint<?, ? extends Annotation>> {

	private final Method method;

	private final Map<Class<?>, MethodMetaData> metaDataByDefiningType = newHashMap();

	public AggregatedMethodMetaData(MethodMetaData metaData) {

		this.method = metaData.getMethod();
		metaDataByDefiningType.put( metaData.getMethod().getDeclaringClass(), metaData );
	}

	public Method getMethod() {
		return method;
	}

	public void addMetaData(MethodMetaData metaData) {
		metaDataByDefiningType.put( metaData.getMethod().getDeclaringClass(), metaData );
	}

	/**
	 * Whether a cascaded validation of the return value of the represented
	 * method shall be performed or not. This is the case if either the method
	 * itself is annotated with {@link Valid} or any of the method's up in the
	 * inheritance hierarchy which it overrides.
	 *
	 * @return <code>True</code>, if a cascaded return value validation shall be
	 *         performed, <code>false</code> otherwise.
	 */
	public boolean isCascading() {

		for ( MethodMetaData oneMethodMetaData : metaDataByDefiningType.values() ) {
			if ( oneMethodMetaData.isCascading() ) {
				return true;
			}
		}

		return false;
	}

	public boolean isConstrained() {

		for ( MethodMetaData oneMethodMetaData : metaDataByDefiningType.values() ) {
			if ( oneMethodMetaData.isConstrained() ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a single method meta data from this aggregation.
	 *
	 * @param method The method to retrieve the meta data for. Must either be the
	 * method represented by this meta data object or one method from
	 * a super-type, which the method represented by this meta data
	 * object overrides/implements.
	 *
	 * @return The meta data for the given method or null if this aggregation
	 *         doesn't contain any meta data for that method.
	 */
	public MethodMetaData getMetaDataForMethod(Method method) {
		return metaDataByDefiningType.get( method.getDeclaringClass() );
	}

	/**
	 * TODO GM: If possible remove; I think, this shouldn't be required by clients.
	 */
	@Deprecated
	public Map<Class<?>, MethodMetaData> getMetaDataByDefiningType() {
		return metaDataByDefiningType;
	}

	/**
	 * An iterator with the return value constraints of the represented method.
	 */
	public Iterator<BeanMetaConstraint<?, ? extends Annotation>> iterator() {

		List<BeanMetaConstraint<?, ? extends Annotation>> theValue = newArrayList();

		for ( MethodMetaData oneMethodMetaData : metaDataByDefiningType.values() ) {
			for ( BeanMetaConstraint<?, ? extends Annotation> oneConstraint : oneMethodMetaData ) {
				theValue.add( oneConstraint );
			}
		}

		return theValue.iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( method == null ) ? 0 : method.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		AggregatedMethodMetaData other = (AggregatedMethodMetaData) obj;
		if ( method == null ) {
			if ( other.method != null ) {
				return false;
			}
		}
		else if ( !method.equals( other.method ) ) {
			return false;
		}
		return true;
	}

}
