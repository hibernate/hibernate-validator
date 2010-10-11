package org.hibernate.validator.test;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.MessageInterpolator;
import org.hibernate.validator.Validator;

/**
 * @author Gavin King
 */
public class ValidatorTest extends TestCase {
	public static final String ESCAPING_EL = "(escaping #{el})";

	public void testValidator() {
		Address a = new Address();
		Address.blacklistedZipCode = null;
		a.setCountry( "Australia" );
		a.setZip( "1221341234123" );
		a.setState( "Vic" );
		a.setLine1( "Karbarook Ave" );
		a.setId( 3 );
		ClassValidator<Address> classValidator = new ClassValidator<Address>(
				Address.class, ResourceBundle.getBundle( "messages", Locale.ENGLISH )
		);
		InvalidValue[] validationMessages = classValidator.getInvalidValues( a );
		assertEquals( 2, validationMessages.length ); //static field is tested also
		Address.blacklistedZipCode = "323232";
		a.setZip( null );
		a.setState( "Victoria" );
		validationMessages = classValidator.getInvalidValues( a );
		assertEquals( 2, validationMessages.length );
		validationMessages = classValidator.getInvalidValues( a, "zip" );
		assertEquals( 1, validationMessages.length );
		a.setZip( "3181" );
		a.setState( "NSW" );
		validationMessages = classValidator.getInvalidValues( a );
		assertEquals( 0, validationMessages.length );
		a.setCountry( null );
		validationMessages = classValidator.getInvalidValues( a );
		assertEquals( 1, validationMessages.length );
		a.setInternalValid( false );
		validationMessages = classValidator.getInvalidValues( a );
		assertEquals( 2, validationMessages.length );
		a.setInternalValid( true );
		a.setCountry( "France" );
		a.floor = 4000;
		validationMessages = classValidator.getInvalidValues( a );
		assertEquals( 2, validationMessages.length );
		assertEquals(
				"Floor cannot " + ESCAPING_EL
						+ " be lower that -2 and greater than 50 " + ESCAPING_EL,
				validationMessages[0].getMessage()
		);
	}

	public void testCircularity() throws Exception {
		Brother emmanuel = new Brother();
		emmanuel.setName( "Emmanuel" );
		Address.blacklistedZipCode = "666";
		Address address = new Address();
		address.setInternalValid( true );
		address.setCountry( "France" );
		address.setId( 3 );
		address.setLine1( "Rue des rosiers" );
		address.setState( "NYC" );
		address.setZip( "33333" );
		address.floor = 4;
		emmanuel.setAddress( address );
		Brother christophe = new Brother();
		christophe.setName( "Christophe" );
		christophe.setAddress( address );
		emmanuel.setYoungerBrother( christophe );
		christophe.setElder( emmanuel );
		ClassValidator<Brother> classValidator = new ClassValidator<Brother>( Brother.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( emmanuel );
		assertEquals( 0, invalidValues.length );
		christophe.setName( null );
		invalidValues = classValidator.getInvalidValues( emmanuel );
		assertEquals( 1, invalidValues.length );
		assertEquals( emmanuel, invalidValues[0].getRootBean() );
		assertEquals( "youngerBrother.name", invalidValues[0].getPropertyPath() );
		christophe.setName( "Christophe" );
		address = new Address();
		address.setInternalValid( true );
		address.setCountry( "France" );
		address.setId( 4 );
		address.setLine1( "Rue des plantes" );
		address.setState( "NYC" );
		address.setZip( "33333" );
		address.floor = -100;
		christophe.setAddress( address );
		invalidValues = classValidator.getInvalidValues( emmanuel );
		assertEquals( 1, invalidValues.length );
	}

	public void testAggregationAnnotations() throws Exception {
		Engine eng = new Engine();
		eng.setHorsePower( 23 );
		eng.setSerialNumber( "23-43###4" );
		ClassValidator<Engine> classValidator = new ClassValidator<Engine>( Engine.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( eng );
		assertEquals( 2, invalidValues.length );

		eng.setSerialNumber( "1234-5678-9012");
		invalidValues = classValidator.getInvalidValues( eng );
		assertEquals( 0, invalidValues.length );
	}

	public void testDefaultResourceBundle() throws Exception {
		Tv tv = new Tv();
		tv.serial = "FS";
		tv.name = "TooLong";
		tv.expDate = new Date( new Date().getTime() + 1000 * 60 * 60 * 24 * 10 );
		ClassValidator<Tv> classValidator = new ClassValidator<Tv>( Tv.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( tv );
		assertEquals( 1, invalidValues.length );
		Locale loc = Locale.getDefault();
		if ( loc.toString().startsWith( "en" ) ) {
			assertEquals( "length must be between 0 and 2", invalidValues[0].getMessage() );
		}
		else if ( loc.toString().startsWith( "fr" ) ) {
			String message = invalidValues[0].getMessage();
			String message2 ="la longueur doit être entre 0 et 2"; 
			assertEquals( message2, message );
		}
		else if ( loc.toString().startsWith( "da" ) ) {
			assertEquals( "længden skal være mellem 0 og 2", invalidValues[0].getMessage() );
		}
		else {
			// if default not found then it must be english
			assertEquals( "length must be between 0 and 2", invalidValues[0].getMessage() );
		}
	}

	public void testSerialization() throws Exception {

		Tv tv = new Tv();
		tv.serial = "FS";
		tv.name = "TooLong";
		tv.expDate = new Date( new Date().getTime() + 1000 * 60 * 60 * 24 * 10 );
		ClassValidator<Tv> classValidator = new ClassValidator<Tv>( Tv.class );

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream( stream );
		out.writeObject( classValidator );
		out.close();
		byte[] serialized = stream.toByteArray();
		stream.close();
		ByteArrayInputStream byteIn = new ByteArrayInputStream( serialized );
		ObjectInputStream in = new ObjectInputStream( byteIn );
		classValidator  = (ClassValidator<Tv>) in.readObject();
		in.close();
		byteIn.close();


		InvalidValue[] invalidValues = classValidator.getInvalidValues( tv );
		assertEquals( 1, invalidValues.length );
		Locale loc = Locale.getDefault();
		if ( loc.toString().startsWith( "en" ) ) {
			assertEquals( "length must be between 0 and 2", invalidValues[0].getMessage() );
		}
		else if ( loc.toString().startsWith( "fr" ) ) {
			String message = invalidValues[0].getMessage();
			String message2 ="la longueur doit être entre 0 et 2";
			assertEquals( message2, message );
		}
		else if ( loc.toString().startsWith( "da" ) ) {
			assertEquals( "længden skal være mellem 0 og 2", invalidValues[0].getMessage() );
		}
		else {
			// if default not found then it must be english
			assertEquals( "length must be between 0 and 2", invalidValues[0].getMessage() );
		}
	}

	public class PrefixMessageInterpolator implements MessageInterpolator, Serializable {
		private String prefix;

		public PrefixMessageInterpolator(String prefix) {
			this.prefix = prefix;
		}

		public String interpolate(String message, Validator validator, MessageInterpolator defaultInterpolator) {
			return prefix + defaultInterpolator.interpolate( message, validator, defaultInterpolator );
		}
	}

	public void testMessageInterpolator() throws Exception {
		Tv tv = new Tv();
		tv.serial = "FS";
		tv.name = "TooLong";
		tv.expDate = new Date( new Date().getTime() + 1000 * 60 * 60 * 24 * 10 );
		String prefix = "Prefix";
		ClassValidator<Tv> classValidator = new ClassValidator<Tv>( Tv.class, new PrefixMessageInterpolator( prefix )  );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( tv );
		assertEquals( 1, invalidValues.length );
		Locale loc = Locale.getDefault();
		if ( loc.toString().startsWith( "en" ) ) {
			assertEquals( prefix + "length must be between 0 and 2", invalidValues[0].getMessage() );
		}
		else if ( loc.toString().startsWith( "fr" ) ) {
			assertEquals( prefix + "la longueur doit être entre 0 et 2", invalidValues[0].getMessage() );
		}
		else if ( loc.toString().startsWith( "da" ) ) {
			assertEquals( prefix + "længden skal være mellem 0 og 2", invalidValues[0].getMessage() );
		}
		else {
			// if default not found then it must be english
			assertEquals( prefix + "length must be between 0 and 2", invalidValues[0].getMessage() );
		}
	}

	public void testBigInteger() throws Exception {
		Tv tv = new Tv();
		tv.lifetime = new BigInteger("9223372036854775808");
		ClassValidator<Tv> classValidator = new ClassValidator<Tv>( Tv.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( tv );
		assertEquals( 0, invalidValues.length );
	}

	public void testBeanValidator() throws Exception {
		Vase v = new Vase();
		ClassValidator<Vase> classValidator = new ClassValidator<Vase>( Vase.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( v );
		assertEquals( 1, invalidValues.length );
		assertNull( invalidValues[0].getPropertyName() );
	}

	public void testPotentialInvalidValues() throws Exception {
		Address a = new Address();
		Address.blacklistedZipCode = null;
		a.setCountry( "Australia" );
		a.setZip( "1221341234123" );
		a.setState( "Vic" );
		a.setLine1( "Karbarook Ave" );
		a.setId( 3 );
		ClassValidator<Address> classValidator = new ClassValidator<Address>( Address.class );
		InvalidValue[] validationMessages = classValidator.getPotentialInvalidValues(
				"blacklistedZipCode", Address.blacklistedZipCode
		);
		assertEquals( 1, validationMessages.length );
	}

	public void testRecursivity() throws Exception {
		Site site = new Site();
		site.setSiteName( "Rocky mountains" );
		Contact contact = new Contact();
		contact.setName( null ); //1 error
		contact.setPhone( "01234455" );
		site.setContact( contact );
		Address a = new Address();
		Address.blacklistedZipCode = "222";
		a.setCountry( "Australia" );
		a.setZip( "123" );
		a.setState( "Vic" );
		a.setLine1( null ); // 1 more error
		a.setId( 3 );
		site.setAddress( a );
		ClassValidator<Site> classValidator = new ClassValidator<Site>( Site.class );
		InvalidValue[] validationMessages = classValidator.getInvalidValues( site );
		assertEquals( 2, validationMessages.length );
	}
}
