/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.descriptor;

import javax.validation.metadata.GroupConversionDescriptor;

/**
 * Describes a group conversion rule.
 *
 * @author Gunnar Morling
 */
public class GroupConversionDescriptorImpl implements GroupConversionDescriptor {

	private final Class<?> from;
	private final Class<?> to;

	public GroupConversionDescriptorImpl(Class<?> from, Class<?> to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public Class<?> getFrom() {
		return from;
	}

	@Override
	public Class<?> getTo() {
		return to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( from == null ) ? 0 : from.hashCode() );
		result = prime * result + ( ( to == null ) ? 0 : to.hashCode() );
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
		GroupConversionDescriptorImpl other = (GroupConversionDescriptorImpl) obj;
		if ( from == null ) {
			if ( other.from != null ) {
				return false;
			}
		}
		else if ( !from.equals( other.from ) ) {
			return false;
		}
		if ( to == null ) {
			if ( other.to != null ) {
				return false;
			}
		}
		else if ( !to.equals( other.to ) ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "GroupConversionDescriptorImpl [from=" + from + ", to=" + to
				+ "]";
	}
}
