/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.path;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.BeanNode;
import javax.validation.Path.ConstructorNode;
import javax.validation.Path.CrossParameterNode;
import javax.validation.Path.MethodNode;
import javax.validation.Path.ParameterNode;
import javax.validation.Path.PropertyNode;
import javax.validation.Path.ReturnValueNode;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Immutable implementation of a {@code Path.Node}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class NodeImpl
		implements Path.PropertyNode, Path.MethodNode, Path.ConstructorNode, Path.BeanNode, Path.ParameterNode, Path.ReturnValueNode, Path.CrossParameterNode, org.hibernate.validator.path.PropertyNode, Serializable {
	private static final long serialVersionUID = 2075466571633860499L;
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[]{};

	private static final Log log = LoggerFactory.make();

	private static final String INDEX_OPEN = "[";
	private static final String INDEX_CLOSE = "]";
	private static final String RETURN_VALUE_NODE_NAME = "<return value>";
	private static final String CROSS_PARAMETER_NODE_NAME = "<cross-parameter>";

	private final String name;
	private final NodeImpl parent;
	private final boolean isIterable;
	private final Integer index;
	private final Object key;
	private final ElementKind kind;
	private final int hashCode;

	//type-specific attributes
	private final Class<?>[] parameterTypes;
	private final Integer parameterIndex;
	private final Object value;

	private String asString;

	private NodeImpl(String name, NodeImpl parent, boolean indexable, Integer index, Object key, ElementKind kind, Class<?>[] parameterTypes, Integer parameterIndex, Object value) {
		this.name = name;
		this.parent = parent;
		this.index = index;
		this.key = key;
		this.value = value;
		this.isIterable = indexable;
		this.kind = kind;
		this.parameterTypes = parameterTypes;
		this.parameterIndex = parameterIndex;
		this.hashCode = buildHashCode();
	}

	//TODO It would be nicer if we could return PropertyNode
	public static NodeImpl createPropertyNode(String name, NodeImpl parent) {
		return new NodeImpl(
				name,
				parent,
				false,
				null,
				null,
				ElementKind.PROPERTY,
				EMPTY_CLASS_ARRAY,
				null,
				null
		);
	}

	public static NodeImpl createParameterNode(String name, NodeImpl parent, int parameterIndex) {
		return new NodeImpl(
				name,
				parent,
				false,
				null,
				null,
				ElementKind.PARAMETER,
				EMPTY_CLASS_ARRAY,
				parameterIndex,
				null
		);
	}

	public static NodeImpl createCrossParameterNode(NodeImpl parent) {
		return new NodeImpl(
				CROSS_PARAMETER_NODE_NAME,
				parent,
				false,
				null,
				null,
				ElementKind.CROSS_PARAMETER,
				EMPTY_CLASS_ARRAY,
				null,
				null
		);
	}

	public static NodeImpl createMethodNode(String name, NodeImpl parent, Class<?>[] parameterTypes) {
		return new NodeImpl( name, parent, false, null, null, ElementKind.METHOD, parameterTypes, null, null );
	}

	public static NodeImpl createConstructorNode(String name, NodeImpl parent, Class<?>[] parameterTypes) {
		return new NodeImpl( name, parent, false, null, null, ElementKind.CONSTRUCTOR, parameterTypes, null, null );
	}

	public static NodeImpl createBeanNode(NodeImpl parent) {
		return new NodeImpl(
				null,
				parent,
				false,
				null,
				null,
				ElementKind.BEAN,
				EMPTY_CLASS_ARRAY,
				null,
				null
		);
	}

	public static NodeImpl createReturnValue(NodeImpl parent) {
		return new NodeImpl(
				RETURN_VALUE_NODE_NAME,
				parent,
				false,
				null,
				null,
				ElementKind.RETURN_VALUE,
				EMPTY_CLASS_ARRAY,
				null,
				null
		);
	}

	public static NodeImpl makeIterable(NodeImpl node) {
		return new NodeImpl(
				node.name,
				node.parent,
				true,
				null,
				null,
				node.kind,
				node.parameterTypes,
				node.parameterIndex,
				node.value

		);
	}

	public static NodeImpl setIndex(NodeImpl node, Integer index) {
		return new NodeImpl(
				node.name,
				node.parent,
				true,
				index,
				null,
				node.kind,
				node.parameterTypes,
				node.parameterIndex,
				node.value
		);
	}

	public static NodeImpl setMapKey(NodeImpl node, Object key) {
		return new NodeImpl(
				node.name,
				node.parent,
				true,
				null,
				key,
				node.kind,
				node.parameterTypes,
				node.parameterIndex,
				node.value
		);
	}

	public static NodeImpl setPropertyValue(NodeImpl node, Object value) {
		return new NodeImpl(
				node.name,
				node.parent,
				node.isIterable,
				node.index,
				node.key,
				node.kind,
				node.parameterTypes,
				node.parameterIndex,
				value
		);
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final boolean isInIterable() {
		return parent != null && parent.isIterable();
	}

	public final boolean isIterable() {
		return isIterable;
	}

	@Override
	public final Integer getIndex() {
		if ( parent == null ) {
			return null;
		}
		else {
			return parent.index;
		}
	}

	@Override
	public final Object getKey() {
		if ( parent == null ) {
			return null;
		}
		else {
			return parent.key;
		}
	}

	public final NodeImpl getParent() {
		return parent;
	}

	@Override
	public ElementKind getKind() {
		return kind;
	}

	@Override
	public <T extends Path.Node> T as(Class<T> nodeType) {
		if ( ( kind == ElementKind.BEAN && nodeType == BeanNode.class ) ||
				( kind == ElementKind.CONSTRUCTOR && nodeType == ConstructorNode.class ) ||
				( kind == ElementKind.CROSS_PARAMETER && nodeType == CrossParameterNode.class ) ||
				( kind == ElementKind.METHOD && nodeType == MethodNode.class ) ||
				( kind == ElementKind.PARAMETER && nodeType == ParameterNode.class ) ||
				( kind == ElementKind.PROPERTY && ( nodeType == PropertyNode.class || nodeType == org.hibernate.validator.path.PropertyNode.class ) ) ||
				( kind == ElementKind.RETURN_VALUE && nodeType == ReturnValueNode.class ) ) {
			return nodeType.cast( this );
		}

		throw log.getUnableToNarrowNodeTypeException( this.getClass().getName(), kind, nodeType.getName() );
	}

	@Override
	public List<Class<?>> getParameterTypes() {
		return Arrays.asList( parameterTypes );
	}

	@Override
	public int getParameterIndex() {
		Contracts.assertTrue(
				kind == ElementKind.PARAMETER,
				"getParameterIndex() may only be invoked for nodes of ElementKind.PARAMETER."
		);
		return parameterIndex.intValue();
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return asString();
	}

	public final String asString() {
		if ( asString == null ) {
			asString = buildToString();
		}
		return asString;
	}

	private String buildToString() {
		StringBuilder builder = new StringBuilder();

		if ( ElementKind.BEAN.equals( getKind() ) ) {
			// class level constraints don't contribute to path
			builder.append( "" );
		}
		else {
			builder.append( getName() );
		}

		if ( isIterable() ) {
			builder.append( INDEX_OPEN );
			if ( index != null ) {
				builder.append( index );
			}
			else if ( key != null ) {
				builder.append( key );
			}
			builder.append( INDEX_CLOSE );
		}
		return builder.toString();
	}

	public int buildHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( index == null ) ? 0 : index.hashCode() );
		result = prime * result + ( isIterable ? 1231 : 1237 );
		result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
		result = prime * result + ( ( kind == null ) ? 0 : kind.hashCode() );
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
		result = prime * result + ( ( parameterIndex == null ) ? 0 : parameterIndex.hashCode() );
		result = prime * result + ( ( parameterTypes == null ) ? 0 : parameterTypes.hashCode() );
		result = prime * result + ( ( parent == null ) ? 0 : parent.hashCode() );
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode;
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
		NodeImpl other = (NodeImpl) obj;
		if ( index == null ) {
			if ( other.index != null ) {
				return false;
			}
		}
		else if ( !index.equals( other.index ) ) {
			return false;
		}
		if ( isIterable != other.isIterable ) {
			return false;
		}
		if ( key == null ) {
			if ( other.key != null ) {
				return false;
			}
		}
		else if ( !key.equals( other.key ) ) {
			return false;
		}
		if ( kind != other.kind ) {
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
		if ( parameterIndex == null ) {
			if ( other.parameterIndex != null ) {
				return false;
			}
		}
		else if ( !parameterIndex.equals( other.parameterIndex ) ) {
			return false;
		}
		if ( parameterTypes == null ) {
			if ( other.parameterTypes != null ) {
				return false;
			}
		}
		else if ( !parameterTypes.equals( other.parameterTypes ) ) {
			return false;
		}
		if ( parent == null ) {
			if ( other.parent != null ) {
				return false;
			}
		}
		else if ( !parent.equals( other.parent ) ) {
			return false;
		}
		return true;
	}
}
