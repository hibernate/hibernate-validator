/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.classchecks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Represents an inheritance tree of overridden methods. In the head of the tree a node from which we start looking for
 * overridden methods is located. Also contains some useful methods to walk around overridden methods.
 *
 * @author Marko Bekhta
 */
public class InheritanceTree implements Iterable<ExecutableElement> {

	// Head element of the tree
	private final TreeNode head;
	// A cache map used to quickly access nodes
	private final HashMap<Name, TreeNode> nodeCache;

	/**
	 * Creates an {@link InheritanceTree} using given parameters as a head element.
	 *
	 * @param element a method in the head of hierarchy
	 * @param enclosingType a type that is enclosing given method
	 */
	public InheritanceTree(ExecutableElement element, TypeElement enclosingType) {
		head = new TreeNode( element, enclosingType );
		nodeCache = CollectionHelper.newHashMap();
		nodeCache.put( enclosingType.getQualifiedName(), head );
	}

	/**
	 * Adds a new node to inheritance tree under the head element.
	 *
	 * @param element a method we want to add
	 * @param enclosingType a type that is enclosing given method
	 */
	public void addNode(ExecutableElement element, TypeElement enclosingType) {
		addNode( element, enclosingType, head.getEnclosingType() );
	}

	/**
	 * Adds a new node to inheritance tree.
	 *
	 * @param element a method we want to add
	 * @param enclosingType a type that is enclosing given method
	 * @param underType under which type we want to add a new node
	 */
	public void addNode(ExecutableElement element, TypeElement enclosingType, TypeElement underType) {
		TreeNode parentNode = nodeCache.get( underType.getQualifiedName() );
		if ( parentNode == null ) {
			throw new IllegalArgumentException( String.format(
					"You cannot add an element under %s as there's no such type in the tree!",
					underType.getQualifiedName()
			) );
		}

		TreeNode nodeToAdd = new TreeNode( element, enclosingType );
		parentNode.addLeaf( nodeToAdd );
		nodeCache.put( enclosingType.getQualifiedName(), nodeToAdd );
	}

	/**
	 * Checks if there are any overridden methods in the hierarchy.
	 *
	 * @return {@code true} if there's any overridden method found, {@code false} otherwise
	 */
	public boolean hasOverriddenMethods() {
		return nodeCache.size() > 1;
	}

	/**
	 * Provides a collection of all overridden methods. Method in the head of hierarchy is excluded as it is not an
	 * overridden one.
	 *
	 * @return a collection of overridden methods
	 */
	public Collection<ExecutableElement> getAllOverriddenMethodsWithoutHead() {
		List<ExecutableElement> elements = CollectionHelper.newArrayList();
		for ( TreeNode treeNode : nodeCache.values() ) {
			if ( treeNode.equals( head ) ) {
				continue;
			}
			elements.add( treeNode.getMethod() );
		}
		return elements;
	}

	/**
	 * Provides a collection of overridden methods that are not overriding another method.
	 *
	 * @return a collection of originally declared methods
	 */
	public Collection<ExecutableElement> getAllOriginallyDeclaredMethods() {
		List<ExecutableElement> elements = CollectionHelper.newArrayList();
		for ( TreeNode treeNode : nodeCache.values() ) {
			if ( treeNode.isLeaf() ) {
				elements.add( treeNode.getMethod() );
			}
		}
		return elements;
	}

	/**
	 * Provides an iterator ({@link Iterator} to go through all methods present in the hierarchy, all overridden ones as
	 * well as the one in the head of the tree.
	 *
	 * @return an iterator
	 */
	@Override
	public Iterator<ExecutableElement> iterator() {
		return new Iterator<ExecutableElement>() {

			private Iterator<TreeNode> nodeIterator = nodeCache.values().iterator();

			@Override
			public boolean hasNext() {
				return nodeIterator.hasNext();
			}

			@Override
			public ExecutableElement next() {
				return nodeIterator.next().getMethod();
			}
		};
	}

	/**
	 * Tree node element that contains information about the method, enclosing type and its leafs.
	 */
	private static class TreeNode {
		private ExecutableElement method;
		private TypeElement enclosingType;
		private List<TreeNode> leafs;

		public TreeNode(ExecutableElement method, TypeElement enclosingType) {
			this.method = method;
			this.enclosingType = enclosingType;
			this.leafs = CollectionHelper.newArrayList();
		}

		/**
		 * Checks if there are any other nodes under this one.
		 *
		 * @return {@code true} if there are any nodes present under current one, {@code false} otherwise
		 */
		public boolean isLeaf() {
			return leafs.isEmpty();
		}

		public ExecutableElement getMethod() {
			return method;
		}

		public List<TreeNode> getLeafs() {
			return leafs;
		}

		public TypeElement getEnclosingType() {
			return enclosingType;
		}

		/**
		 * Adds a node under the current one.
		 *
		 * @param node node to add
		 */
		public void addLeaf(TreeNode node) {
			leafs.add( node );
		}
	}
}
