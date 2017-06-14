/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.BeanNode;
import javax.validation.Path.ContainerElementNode;
import javax.validation.Path.PropertyNode;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.testng.annotations.Test;

/**
 * Used to print the assertions shown in table 6.1/6.2 of the spec.
 *
 * @author Gunnar Morling
 *
 */
public class SpecExamplePropertyPathTest {

	/**
	 * 9.) book.tags
	 */
	@Test
	public void containerElementConstraint() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );
		book.setAuthors( Collections.emptyList() );
		book.setTags( Arrays.asList( "some tag", "", "another tag" ) );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NotBlank.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 10.) book.tagsByChapter
	 */
	@Test
	public void nestedContainerElementConstraint() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );
		book.setAuthors( Collections.emptyList() );

		Map<Integer, List<String>> tagsByChapter = new HashMap<>();
		tagsByChapter.put( 4, Arrays.asList( "some tag", "another tag", "" ) );
		book.setTagsByChapter( tagsByChapter  );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NotBlank.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 11.) book.categories.name
	 */
	@Test
	public void cascadedValidationWithPropertyConstraint() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );
		book.setAuthors( Collections.emptyList() );
		book.setCategories( Arrays.asList( new Category( "long enough" ), new Category( "a" ), new Category( "long enough" ) ) );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( Size.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 12.) book.authorsByChapter.name
	 */
	@Test
	public void nestedCascadedValidationWithPropertyConstraint() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );
		book.setAuthors( Collections.emptyList() );

		Map<Integer, List<Author>> authorsByChapter = new HashMap<>();
		authorsByChapter.put( 4, Arrays.asList( new Author( "Bob" ), new Author( "Bruce" ), new Author( "" ) ) );
		book.setAuthorsByChapter( authorsByChapter  );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NotEmpty.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	private void printNode(Path.Node node) {
		StringBuilder sb = new StringBuilder();

		switch ( node.getKind() ) {
			case BEAN:
				sb.append( "BeanNode" );
				break;
			case CONSTRUCTOR:
				sb.append( "ConstructorNode" );
				break;
			case CONTAINER_ELEMENT:
				sb.append( "ContainerElementNode" );
				break;
			case CROSS_PARAMETER:
				sb.append( "CrossParameterNode" );
				break;
			case METHOD:
				sb.append( "MethodNode" );
				break;
			case PARAMETER:
				sb.append( "ParameterNode" );
				break;
			case PROPERTY:
				sb.append( "PropertyNode" );
				break;
			case RETURN_VALUE:
				sb.append( "ReturnValueNode" );
				break;
			default:
				break;

		}
		sb.append( "(" );
		sb.append( "name=" );
		sb.append( node.getName() );
		sb.append( ", inIterable=" );
		sb.append( node.isInIterable() );
		sb.append( ", index=" );
		sb.append( node.getIndex() );
		sb.append( ", key=" );
		sb.append( node.getKey() );

		if ( node.getKind() == ElementKind.BEAN ) {
			sb.append( ", containerClass=" );
			sb.append( toString( ( (BeanNode) node ).getContainerClass() ) );
			sb.append( ", typeArgumentIndex=" );
			sb.append( ( (BeanNode) node ).getTypeArgumentIndex() );
		}
		else if ( node.getKind() == ElementKind.PROPERTY ) {
			sb.append( ", containerClass=" );
			sb.append( toString( ( (PropertyNode) node ).getContainerClass() ) );
			sb.append( ", typeArgumentIndex=" );
			sb.append( ( (PropertyNode) node ).getTypeArgumentIndex() );
		}
		else if ( node.getKind() == ElementKind.CONTAINER_ELEMENT ) {
			sb.append( ", containerClass=" );
			sb.append( toString( ( (ContainerElementNode) node ).getContainerClass() ) );
			sb.append( ", typeArgumentIndex=" );
			sb.append( ( (ContainerElementNode) node ).getTypeArgumentIndex() );
		}
		sb.append( ", kind=ElementKind." );
		sb.append( node.getKind() );
		sb.append( ")" );
		sb.append( System.lineSeparator() );

		System.out.println( sb.toString() );
	}

	private String toString(Class<?> optionalClass) {
		return optionalClass != null ? optionalClass.getSimpleName() + ".class" : null;
	}
}
