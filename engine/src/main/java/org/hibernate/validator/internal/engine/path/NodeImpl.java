/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.path;

import java.io.Serial;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Path.BeanNode;
import jakarta.validation.Path.ConstructorNode;
import jakarta.validation.Path.ContainerElementNode;
import jakarta.validation.Path.CrossParameterNode;
import jakarta.validation.Path.MethodNode;
import jakarta.validation.Path.ParameterNode;
import jakarta.validation.Path.PropertyNode;
import jakarta.validation.Path.ReturnValueNode;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Immutable implementation of a {@code Path.Node}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class NodeImpl
		implements Path.PropertyNode, Path.MethodNode, Path.ConstructorNode, Path.BeanNode, Path.ParameterNode, Path.ReturnValueNode, Path.CrossParameterNode, Path.ContainerElementNode,
		org.hibernate.validator.path.PropertyNode, org.hibernate.validator.path.ContainerElementNode, Serializable {
	@Serial
	private static final long serialVersionUID = 2075466571633860499L;
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[] { };

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	static final NodeImpl ROOT_NODE;

	static {
		ROOT_NODE = NodeImpl.createBeanNode( null );
		ROOT_NODE.valueSet = true;
		ROOT_NODE.nodes = new NodeImpl[] { ROOT_NODE };
		ROOT_NODE.hashCode();
	}

	private static final String INDEX_OPEN = "[";
	private static final String INDEX_CLOSE = "]";
	private static final String TYPE_PARAMETER_OPEN = "<";
	private static final String TYPE_PARAMETER_CLOSE = ">";

	public static final String RETURN_VALUE_NODE_NAME = "<return value>";
	public static final String CROSS_PARAMETER_NODE_NAME = "<cross-parameter>";
	public static final String ITERABLE_ELEMENT_NODE_NAME = "<iterable element>";
	public static final String LIST_ELEMENT_NODE_NAME = "<list element>";
	public static final String MAP_KEY_NODE_NAME = "<map key>";
	public static final String MAP_VALUE_NODE_NAME = "<map value>";

	private final String name;
	private final NodeImpl parent;
	private final NodeImpl root;
	private final int size;
	private boolean isIterable;
	private final Integer index;
	private final Object key;
	private final ElementKind kind;

	//type-specific attributes
	private final Class<?>[] parameterTypes;
	private final Integer parameterIndex;
	private Object value;
	private boolean valueSet;
	private final Class<?> containerClass;
	private final Integer typeArgumentIndex;

	private int hashCode = -1;
	private String asString;
	private NodeImpl[] nodes;

	private NodeImpl(
			String name, NodeImpl parent, boolean isIterable, Integer index, Object key, ElementKind kind, Class<?>[] parameterTypes,
			Integer parameterIndex, Object value, boolean valueSet, Class<?> containerClass, Integer typeArgumentIndex
	) {
		this.name = name;
		this.parent = parent;
		this.root = parent == null ? this : parent.root;
		this.size = ( parent == null ? 0 : parent.size ) + 1;
		this.index = index;
		this.key = key;
		this.value = value;
		this.valueSet = valueSet;
		this.isIterable = isIterable;
		this.kind = kind;
		this.parameterTypes = parameterTypes;
		this.parameterIndex = parameterIndex;
		this.containerClass = containerClass;
		this.typeArgumentIndex = typeArgumentIndex;
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
				null,
				false,
				null,
				null
		);
	}

	public static NodeImpl createContainerElementNode(String name, NodeImpl parent) {
		return new NodeImpl(
				name,
				parent,
				false,
				null,
				null,
				ElementKind.CONTAINER_ELEMENT,
				EMPTY_CLASS_ARRAY,
				null,
				null,
				false,
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
				null,
				false,
				null,
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
				null,
				false,
				null,
				null
		);
	}

	public static NodeImpl createMethodNode(String name, NodeImpl parent, Class<?>[] parameterTypes) {
		return new NodeImpl( name, parent, false, null, null, ElementKind.METHOD, parameterTypes, null, null, false, null, null );
	}

	public static NodeImpl createConstructorNode(String name, NodeImpl parent, Class<?>[] parameterTypes) {
		return new NodeImpl( name, parent, false, null, null, ElementKind.CONSTRUCTOR, parameterTypes, null, null, false, null, null );
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
				null,
				false,
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
				null,
				false,
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
				node.value,
				node.valueSet,
				node.containerClass,
				node.typeArgumentIndex
		);
	}

	public static NodeImpl makeIterableAndSetIndex(NodeImpl node, Integer index) {
		return new NodeImpl(
				node.name,
				node.parent,
				true,
				index,
				null,
				node.kind,
				node.parameterTypes,
				node.parameterIndex,
				node.value,
				node.valueSet,
				node.containerClass,
				node.typeArgumentIndex
		);
	}

	public static NodeImpl makeIterableAndSetMapKey(NodeImpl node, Object key) {
		return new NodeImpl(
				node.name,
				node.parent,
				true,
				null,
				key,
				node.kind,
				node.parameterTypes,
				node.parameterIndex,
				node.value,
				node.valueSet,
				node.containerClass,
				node.typeArgumentIndex
		);
	}

	public static NodeImpl setPropertyValue(NodeImpl node, Object value) {
		if ( node.valueSet && node.value != value ) {
			return new NodeImpl(
					node.name,
					node.parent,
					node.isIterable,
					node.index,
					node.key,
					node.kind,
					node.parameterTypes,
					node.parameterIndex,
					value,
					true,
					node.containerClass,
					node.typeArgumentIndex
			);
		}
		node.value = value;
		node.valueSet = true;
		return node;
	}

	public static NodeImpl setTypeParameter(NodeImpl node, Class<?> containerClass, Integer typeArgumentIndex) {
		if ( node.typeArgumentIndex != null && node.typeArgumentIndex.equals( typeArgumentIndex ) && node.containerClass == containerClass ) {
			return node;
		}
		return new NodeImpl(
				node.name,
				node.parent,
				node.isIterable,
				node.index,
				node.key,
				node.kind,
				node.parameterTypes,
				node.parameterIndex,
				node.value,
				node.valueSet,
				containerClass,
				typeArgumentIndex
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

	@Override
	public Class<?> getContainerClass() {
		Contracts.assertTrue(
				kind == ElementKind.BEAN || kind == ElementKind.PROPERTY || kind == ElementKind.CONTAINER_ELEMENT,
				"getContainerClass() may only be invoked for nodes of type ElementKind.BEAN, ElementKind.PROPERTY or ElementKind.CONTAINER_ELEMENT."
		);
		if ( parent == null ) {
			return null;
		}
		return parent.containerClass;
	}

	@Override
	public Integer getTypeArgumentIndex() {
		Contracts.assertTrue(
				kind == ElementKind.BEAN || kind == ElementKind.PROPERTY || kind == ElementKind.CONTAINER_ELEMENT,
				"getTypeArgumentIndex() may only be invoked for nodes of type ElementKind.BEAN, ElementKind.PROPERTY or ElementKind.CONTAINER_ELEMENT."
		);
		if ( parent == null ) {
			return null;
		}
		return parent.typeArgumentIndex;
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
		if ( ( kind == ElementKind.BEAN && nodeType == BeanNode.class )
				|| ( kind == ElementKind.CONSTRUCTOR && nodeType == ConstructorNode.class ) || ( kind == ElementKind.CROSS_PARAMETER && nodeType == CrossParameterNode.class )
				|| ( kind == ElementKind.METHOD && nodeType == MethodNode.class ) || ( kind == ElementKind.PARAMETER && nodeType == ParameterNode.class )
				|| ( kind == ElementKind.PROPERTY && ( nodeType == PropertyNode.class || nodeType == org.hibernate.validator.path.PropertyNode.class ) )
				|| ( kind == ElementKind.RETURN_VALUE && nodeType == ReturnValueNode.class )
				|| ( kind == ElementKind.CONTAINER_ELEMENT && ( nodeType == ContainerElementNode.class || nodeType == org.hibernate.validator.path.ContainerElementNode.class ) ) ) {
			return nodeType.cast( this );
		}

		throw LOG.getUnableToNarrowNodeTypeException( this.getClass(), kind, nodeType );
	}

	@Override
	public List<Class<?>> getParameterTypes() {
		return Arrays.asList( parameterTypes );
	}

	@Override
	public int getParameterIndex() {
		Contracts.assertTrue(
				kind == ElementKind.PARAMETER,
				"getParameterIndex() may only be invoked for nodes of type ElementKind.PARAMETER."
		);
		return parameterIndex;
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

		if ( getName() != null ) {
			builder.append( getName() );
		}

		if ( includeTypeParameterInformation( containerClass, typeArgumentIndex ) ) {
			builder.append( TYPE_PARAMETER_OPEN );
			builder.append( TypeVariables.getTypeParameterName( containerClass, typeArgumentIndex ) );
			builder.append( TYPE_PARAMETER_CLOSE );
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

	// TODO: this is used to reduce the number of differences until we agree on the string representation
	// it introduces some inconsistent behavior e.g. you get '<V>' for a Multimap but not for a Map
	private static boolean includeTypeParameterInformation(Class<?> containerClass, Integer typeArgumentIndex) {
		if ( containerClass == null || typeArgumentIndex == null ) {
			return false;
		}

		if ( containerClass.getTypeParameters().length < 2 ) {
			return false;
		}
		if ( Map.class.isAssignableFrom( containerClass ) && typeArgumentIndex == 1 ) {
			return false;
		}
		return true;
	}

	public final int buildHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( index == null ) ? 0 : index.hashCode() );
		result = prime * result + ( isIterable ? 1231 : 1237 );
		result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
		result = prime * result + ( ( kind == null ) ? 0 : kind.hashCode() );
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
		result = prime * result + ( ( parameterIndex == null ) ? 0 : parameterIndex.hashCode() );
		result = prime * result + ( ( parameterTypes == null ) ? 0 : Arrays.hashCode( parameterTypes ) );
		result = prime * result + ( ( containerClass == null ) ? 0 : containerClass.hashCode() );
		result = prime * result + ( ( typeArgumentIndex == null ) ? 0 : typeArgumentIndex.hashCode() );
		return result;
	}

	@Override
	public int hashCode() {
		if ( hashCode == -1 ) {
			hashCode = buildHashCode();
		}

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
		return samePath( other );
	}

	boolean sameNode(NodeImpl other) {
		if ( this == other ) {
			return true;
		}
		if ( other == null ) {
			return false;
		}
		if ( hashCode != -1 && other.hashCode != -1 && hashCode != other.hashCode ) {
			return false;
		}
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
		if ( containerClass == null ) {
			if ( other.containerClass != null ) {
				return false;
			}
		}
		else if ( !containerClass.equals( other.containerClass ) ) {
			return false;
		}
		if ( typeArgumentIndex == null ) {
			if ( other.typeArgumentIndex != null ) {
				return false;
			}
		}
		else if ( !typeArgumentIndex.equals( other.typeArgumentIndex ) ) {
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
		else if ( !Arrays.equals( parameterTypes, other.parameterTypes ) ) {
			return false;
		}
		return true;
	}

	boolean samePath(NodeImpl other) {
		if ( this.size != other.size ) {
			return false;
		}
		NodeImpl curr = this;
		NodeImpl otherCurr = other;
		while ( curr != null && otherCurr != null ) {
			if ( !curr.sameNode( otherCurr ) ) {
				return false;
			}
			otherCurr = otherCurr.parent;
			curr = curr.parent;
		}

		return curr == null && otherCurr == null;
	}

	public boolean isRootPath() {
		return parent == null && name == null;
	}

	static NodeImpl[] constructPath(NodeImpl leaf) {
		if ( leaf.parent == null ) {
			if ( leaf.nodes == null ) {
				leaf.nodes = new NodeImpl[] { leaf };
			}
		}
		else {
			leaf.nodes = new NodeImpl[leaf.size - 1];
			NodeImpl curr = leaf;
			while ( curr.parent != null ) {
				leaf.nodes[curr.size - 2] = curr;
				curr = curr.parent;
			}
		}

		return leaf.nodes;
	}

	boolean isSubPathOf(NodeImpl other) {
		if ( this.size > other.size ) {
			return false;
		}
		NodeImpl curr = this;
		NodeImpl otherCurr = other;
		while ( otherCurr != null && !otherCurr.equals( this ) ) {
			otherCurr = otherCurr.parent;
		}
		if ( otherCurr == null ) {
			return false;
		}
		while ( !curr.isRootPath() && !otherCurr.isRootPath() ) {
			if ( !curr.equals( otherCurr ) ) {
				return false;
			}
			curr = curr.parent;
			otherCurr = otherCurr.parent;
		}

		return curr.isRootPath();
	}

	public boolean isSubPathOrContains(NodeImpl other) {
		NodeImpl curr;
		NodeImpl otherCurr;
		if ( this.size > other.size ) {
			curr = other;
			otherCurr = this;
		}
		else {
			curr = this;
			otherCurr = other;
		}

		while ( otherCurr != null && !otherCurr.equals( curr ) ) {
			otherCurr = otherCurr.parent;
		}
		if ( otherCurr == null ) {
			return false;
		}
		while ( !curr.isRootPath() && !otherCurr.isRootPath() ) {
			if ( !curr.equals( otherCurr ) ) {
				return false;
			}
			curr = curr.parent;
			otherCurr = otherCurr.parent;
		}

		return curr.isRootPath() && otherCurr.isRootPath();
	}

	protected static class NodeIterator implements Iterator<Path.Node> {
		private final NodeImpl[] array;
		private int index;

		public NodeIterator(NodeImpl[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public Path.Node next() {
			if ( index < array.length ) {
				return array[index++];
			}
			throw new NoSuchElementException();
		}
	}
}
