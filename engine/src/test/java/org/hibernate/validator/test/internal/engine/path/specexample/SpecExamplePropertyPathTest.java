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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Path.BeanNode;
import jakarta.validation.Path.ConstructorNode;
import jakarta.validation.Path.ContainerElementNode;
import jakarta.validation.Path.MethodNode;
import jakarta.validation.Path.ParameterNode;
import jakarta.validation.Path.PropertyNode;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;

import org.testng.annotations.Test;

/**
 * Used to print the assertions shown in table 6.1/6.2 of the spec.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class SpecExamplePropertyPathTest {

	/**
	 * 1.1.) book class level constraint
	 */
	@Test
	public void classLevelConstraint() {
		Validator validator = getValidator();

		Book book = new Book();

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, Availability.class );

		assertThat( constraintViolations ).containsOnlyViolations( violationOf( AvailableInStore.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.2.) book.title
	 */
	@Test
	public void propertyConstraint1() {
		Validator validator = getValidator();

		Book book = new Book();

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book, FirstLevelCheck.class );

		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NonEmpty.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.3.) book.authors
	 */
	@Test
	public void propertyConstraint2() {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );

		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NotNull.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.4.) book.authors class level constraint
	 */
	@Test
	public void cascadedValidationWithClassLevelConstraint() {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );
		book.setAuthors( Arrays.asList( new Author( "West" ), new Author( "Wayne" ), new Author( "Hood" ), new Author( "Irving", false ) ) );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );

		assertThat( constraintViolations ).containsOnlyViolations( violationOf( SecurityChecking.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.5.) book.author.lastname
	 */
	@Test
	public void cascadedValidationWithPropertyConstraintLegacyStyle() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );
		book.setAuthors( Arrays.asList( new Author( "West" ), new Author( "Wayne" ), new Author( "Hood" ), new Author( "" ) ) );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NonEmpty.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.6.) book.author.company
	 */
	@Test
	public void cascadedValidationWithPropertyConstraintLegacyStyle2() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "A book" );
		book.setAuthors( Arrays.asList( new Author( "John", "West", "A Company Name That Is Way Too Long" ) ) );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( Size.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.7.) book.author.reviewsPerSource
	 */
	@Test
	public void cascadedValidationWithPropertyConstraintLegacyStyle3() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "In One Person" );
		book.setAuthors( Arrays.asList( new Author( "Irving" ) ) );

		Map<String, Review> reviewsPerSource = new HashMap<>();
		reviewsPerSource.put( "Consumer Report", new Review( -5 ) );
		book.setReviewsPerSource( reviewsPerSource );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( Min.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.8.) book.author.pickedReview
	 */
	@Test
	public void cascadedValidationWithPropertyConstraint() throws Exception {
		Validator validator = getValidator();

		Book book = new Book();
		book.setTitle( "In One Person" );
		book.setAuthors( Arrays.asList( new Author( "Irving" ) ) );
		book.setPickedReview( new Review( -5 ) );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( Min.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 1.9.) book.tags
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
	 * 1.10.) book.tagsByChapter
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
	 * 1.11.) book.categories.name
	 */
	@Test
	public void cascadedValidationWithPropertyConstraint2() throws Exception {
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
	 * 1.12.) book.authorsByChapter.name
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
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NonEmpty.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.1.) Library(location)
	 */
	@Test
	public void constructorParameterConstraint() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Constructor<Library> constructor = Library.class.getConstructor( String.class, String.class );

		Set<ConstraintViolation<Library>> constraintViolations = validator.validateConstructorParameters( constructor,
				new Object[]{ "Passages", null } );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NotNull.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.2.) Library#addBook(book)
	 */
	@Test
	public void methodParameterConstraint() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Library library = new Library( "Passages", "Lyon" );
		Method method = Library.class.getMethod( "addBook", Book.class );

		Set<ConstraintViolation<Library>> constraintViolations = validator.validateParameters( library, method, new Object[]{ null } );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NotNull.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.3.) Library#addBook(book.title)
	 */
	@Test
	public void cascadedValidationWithPropertyConstraintOnMethodParameter() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Library library = new Library( "Passages", "Lyon" );
		Method method = Library.class.getMethod( "addBook", Book.class );

		Set<ConstraintViolation<Library>> constraintViolations = validator.validateParameters( library, method,
				new Object[]{ new Book( null, new Author( "Irving" ) ) } );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NonEmpty.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.4.) Library#addAllBooks(books[3].title)
	 */
	@Test
	public void cascadedValidationWithPropertyConstraintInContainerOnMethodParameter() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Library library = new Library( "Passages", "Lyon" );
		Method method = Library.class.getMethod( "addAllBooks", List.class );

		Set<ConstraintViolation<Library>> constraintViolations = validator.validateParameters( library, method,
				new Object[]{ Arrays.asList(
						new Book( "The Water-Method Man", new Author( "Irving" ) ),
						new Book( "The World According to Garp", new Author( "Irving" ) ),
						new Book( "Avenue of Mysteries", new Author( "Irving" ) ),
						new Book( "", new Author( "Irving" ) )
				) }
		);
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NonEmpty.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.5.) Library#getLocation()
	 */
	@Test
	public void methodReturnValueConstraint() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Library library = new Library( "Passages", null );
		Method method = Library.class.getMethod( "getLocation" );

		Set<ConstraintViolation<Library>> constraintViolations = validator.validateReturnValue( library, method, null );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NotNull.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.6.) Library#getMostPopularBookPerAuthor()
	 */
	@Test
	public void cascadedValidationOnMethodReturnValueConstraint() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Library library = new Library( "Passages", "Lyon" );
		Method method = Library.class.getMethod( "getMostPopularBookPerAuthor" );

		Author johnDoe = new Author( "John", "Doe" );
		Map<Author, Book> mostPopularBookPerAuthor = new HashMap<>();
		mostPopularBookPerAuthor.put( johnDoe, new Book( "", johnDoe ) );

		Set<ConstraintViolation<Library>> constraintViolations = validator.validateReturnValue( library, method, mostPopularBookPerAuthor );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NonEmpty.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.7.) Author#renewPassword()
	 */
	@Test
	public void crossParameterConstraint() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Set<ConstraintViolation<Author>> constraintViolations = validator.validateParameters(
				new Author( "John", "Doe" ),
				Author.class.getMethod( "renewPassword", String.class, String.class, String.class ),
				new Object[]{ "foo", "foo", "foo" } );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( OldAndNewPasswordsDifferent.class ) );

		Path path = constraintViolations.iterator().next().getPropertyPath();

		for ( Path.Node node : path ) {
			printNode( node );
		}
	}

	/**
	 * 2.8.) Author#renewPassword()
	 */
	@Test
	public void crossParameterConstraintWithForgedViolation() throws Exception {
		ExecutableValidator validator = getValidator().forExecutables();

		Set<ConstraintViolation<Author>> constraintViolations = validator.validateParameters(
				new Author( "John", "Doe" ),
				Author.class.getMethod( "renewPassword", String.class, String.class, String.class ),
				new Object[]{ "foo", "bar", "baz" } );
		assertThat( constraintViolations ).containsOnlyViolations( violationOf( NewPasswordsIdentical.class ) );

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
			sb.append( toString( node.as( BeanNode.class ).getContainerClass() ) );
			sb.append( ", typeArgumentIndex=" );
			sb.append( node.as( BeanNode.class ).getTypeArgumentIndex() );
		}
		else if ( node.getKind() == ElementKind.PROPERTY ) {
			sb.append( ", containerClass=" );
			sb.append( toString( node.as( PropertyNode.class ).getContainerClass() ) );
			sb.append( ", typeArgumentIndex=" );
			sb.append( node.as( PropertyNode.class ).getTypeArgumentIndex() );
		}
		else if ( node.getKind() == ElementKind.CONTAINER_ELEMENT ) {
			sb.append( ", containerClass=" );
			sb.append( toString( node.as( ContainerElementNode.class ).getContainerClass() ) );
			sb.append( ", typeArgumentIndex=" );
			sb.append( node.as( ContainerElementNode.class ).getTypeArgumentIndex() );
		}

		sb.append( ", kind=ElementKind." );
		sb.append( node.getKind() );

		if ( node.getKind() == ElementKind.CONSTRUCTOR ) {
			sb.append( ", parameterTypes=" );
			sb.append( toString( node.as( ConstructorNode.class ).getParameterTypes() ) );
		}
		else if ( node.getKind() == ElementKind.METHOD ) {
			sb.append( ", parameterTypes=" );
			sb.append( toString( node.as( MethodNode.class ).getParameterTypes() ) );
		}
		else if ( node.getKind() == ElementKind.PARAMETER ) {
			sb.append( ", parameterIndex=" );
			sb.append( node.as( ParameterNode.class ).getParameterIndex() );
		}

		sb.append( ")" );
		sb.append( System.lineSeparator() );

		System.out.println( sb.toString() );
	}

	private String toString(Class<?> optionalClass) {
		return optionalClass != null ? optionalClass.getSimpleName() + ".class" : null;
	}

	private String toString(List<Class<?>> classes) {
		return classes.stream()
				.map( c -> toString( c ) )
				.collect( Collectors.joining( ",", "[", "]" ) );
	}
}
