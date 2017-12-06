package expressions;

import exceptions.OverflowException;
import operations.Operation;

/**
 * Created by Лев on 11.04.2017.
 */
public class Square<T> extends AbstractUnaryOperator<T> {
    public Square(final TripleExpression<T> x, final Operation<T> y) {
        super(x, y);
    }

    protected T apply(final T x) throws OverflowException {
        return op.sqr(x);
    }
}
