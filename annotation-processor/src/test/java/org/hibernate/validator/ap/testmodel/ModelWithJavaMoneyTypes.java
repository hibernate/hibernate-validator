package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.Currency;

import javax.money.MonetaryAmount;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

/**
 * Constraint @{@link org.hibernate.validator.constraints.Currency},
 * appliable on type {@link MonetaryAmount}
 * with {@link org.hibernate.validator.internal.constraintvalidators.bv.money.CurrencyValidatorForMonetaryAmount}.
 * <p>
 * We can also use @{@link DecimalMax}
 * with {@link org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMaxValidatorForMonetaryAmount}.
 * </p>
 */
public class ModelWithJavaMoneyTypes
{

    @Currency("EUR")
    @DecimalMax("1000.00")
    @DecimalMin("0.00")
    public MonetaryAmount monetaryAmount;

}
