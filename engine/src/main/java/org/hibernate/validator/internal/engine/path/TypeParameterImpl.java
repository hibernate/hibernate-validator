/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.path;

import java.lang.reflect.TypeVariable;

import javax.validation.TypeParameter;
import javax.validation.ValidationException;

/**
 * Default implementation of {@code javax.validation.TypeParameter}.
 *
 * @author Guillaume Smet
 */
public class TypeParameterImpl implements TypeParameter {

	private final String name;

	private final int index;

	private GenericDeclaration genericDeclaration;

	public static TypeParameter of(TypeVariable<?> typeVariable) {
		java.lang.reflect.GenericDeclaration originalGenericDeclaration = (java.lang.reflect.GenericDeclaration) typeVariable.getGenericDeclaration();
		if ( !( originalGenericDeclaration instanceof Class ) ) {
			throw new ValidationException( "Only class level type variables are supported." );
		}

		GenericDeclaration genericDeclaration = new ClassGenericDeclarationImpl( (Class<?>) originalGenericDeclaration );
		TypeParameter typeParameter = null;

		for ( TypeParameter currentTypeParameter : genericDeclaration.getTypeParameters() ) {
			((TypeParameterImpl) currentTypeParameter).genericDeclaration = genericDeclaration;
			if ( typeVariable.getName().equals( currentTypeParameter.getName() ) ) {
				typeParameter = currentTypeParameter;
			}
		}

		return typeParameter;
	}

	private TypeParameterImpl(String name, int index) {
		this.name = name;
		this.index = index;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public GenericDeclaration getGenericDeclaration() {
		return genericDeclaration;
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
		TypeParameterImpl other = (TypeParameterImpl) obj;
		if ( index != other.index ) {
			return false;
		}
		if ( name == null ) {
			if ( other.name != null ) {
				return false;
			}
		}
		else if ( !name.equals( other.name ) ) {
			return false;
		}
		if ( genericDeclaration == null ) {
			if ( other.genericDeclaration != null ) {
				return false;
			}
		}
		else if ( !genericDeclaration.equals( other.genericDeclaration ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
		result = prime * result + index;
		result = prime * result + ( ( genericDeclaration == null ) ? 0 : genericDeclaration.hashCode() );
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( name );
		sb.append( " from " );
		sb.append( genericDeclaration );
		return sb.toString();
	}

	/**
	 * Class based generic declaration.
	 */
	public static class ClassGenericDeclarationImpl implements GenericDeclaration {

		private final String className;

		private final TypeParameter[] typeParameters;

		private ClassGenericDeclarationImpl(Class<?> clazz) {
			this.className = clazz.getName();
			this.typeParameters = new TypeParameter[clazz.getTypeParameters().length];
			for ( int i = 0; i < clazz.getTypeParameters().length; i++ ) {
				this.typeParameters[i] = new TypeParameterImpl( clazz.getTypeParameters()[i].getName(), i );
			}
		}

		/**
		 * Returns the fully qualified class name of the class.
		 *
		 * @return the fully qualified class name
		 */
		public String getClassName() {
			return className;
		}

		@Override
		public TypeParameter[] getTypeParameters() {
			return typeParameters;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends GenericDeclaration> T as(Class<T> implementation) {
			if ( !implementation.isAssignableFrom( getClass() ) ) {
				throw new ValidationException( "Cannot cast " + getClass().getName() + " to " + implementation.getName() + "." );
			}
			return (T) this;
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
			ClassGenericDeclarationImpl other = (ClassGenericDeclarationImpl) obj;
			if ( !className.equals( other.className ) ) {
				return false;
			}
			if ( typeParameters.length != other.typeParameters.length ) {
				return false;
			}
			for ( int i = 0; i < typeParameters.length; i++ ) {
				if ( !typeParameters[i].getName().equals( other.typeParameters[i].getName() ) ) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( className == null ) ? 0 : className.hashCode() );
			for ( TypeParameter typeParameter : typeParameters ) {
				result = prime * result + ( ( typeParameter == null ) ? 0 : typeParameter.getName().hashCode() );
			}
			return result;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append( className );
			sb.append( "<" );
			for ( int i = 0; i < typeParameters.length; i++ ) {
				if ( i > 0 ) {
					sb.append( ", " );
				}
				sb.append( typeParameters[i].getName() );
			}
			sb.append( ">" );
			return sb.toString();
		}
	}
}
