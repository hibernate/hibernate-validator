/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package mockservice;

import com.example.CustomerDecimalMin;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import javax.el.ExpressionFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.spi.ValidationProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public final class MockService {
    private MockService() {
        //
    }

    public static Set<ConstraintViolation<CustomerDecimalMin>> run( ClassLoader testClassLoader ) {
        CustomerDecimalMin customer = new CustomerDecimalMin();

        HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class )
                // Cannot use lambda here, as the version of checkstyle in use implodes on it.
                        .providerResolver( new ValidationProviderResolver() {
                            @Override
                            public List<ValidationProvider<?>> getValidationProviders() {
                                ValidationProvider<HibernateValidatorConfiguration> prov = new HibernateValidator();
                                List<ValidationProvider<?>> provs = new ArrayList<>();
                                provs.add( prov );
                                return provs;
                            }
                        }).configure();

        ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();
        ExpressionFactory expressionFactory;
        try {
            // We might have been called under an unfriendly TCCL.
            // Use _our_ classloader, which has com.sun.el and all that, to obtain
            // an expression factory, since ExpressionFactory.newInstance() uses the TCCL.
            Thread.currentThread().setContextClassLoader( MockService.class.getClassLoader() );
            expressionFactory = ExpressionFactory.newInstance();
            configuration.messageInterpolator( new ResourceBundleMessageInterpolator(
                    new PlatformResourceBundleLocator(
                            ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES),
                    expressionFactory ));
        }
        finally {
            Thread.currentThread().setContextClassLoader( oldTccl );
        }

        configuration.externalClassLoader( MockService.class.getClassLoader() );

        try {
            // Use a class loader with nothing in it to build the factory and the
            // validator to ensure; this models the situation when something like pax-web
            // has set the TCCL to a 'boring' class loader that lacks needed things.
            Thread.currentThread().setContextClassLoader( testClassLoader );
            Validator validator = configuration.buildValidatorFactory().getValidator();
            return validator.validate( customer );
        }
        finally {
            Thread.currentThread().setContextClassLoader( oldTccl );
        }
    }
}
