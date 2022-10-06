/*
 * This class is designed to use Complex in order to represent polynomials
 * with complex co-efficients. 
 */

public class Polynomial {
    /**
     * An array storing the complex co-efficients of the polynomial.
     */
    Complex[] coeff;

    // ========================================================
    // Constructor functions.
    // ========================================================

    /**
     * General constructor: assigns this polynomial a given set of
     * co-efficients.
     *
     * @param coeff The co-efficients to use for this polynomial.
     */
    public Polynomial(Complex[] coeff) {

        if (coeff.length == 1 && (coeff[0].getReal() == 0 && coeff[0].getImag() == 0)) {
            this.coeff = coeff;
        } else {
            int offset = 0;
            for (int i = coeff.length - 1; i >= 0; i--) {
                if (coeff[i].getReal() == 0 && coeff[i].getImag() == 0) {
                    offset++;
                }
                break;
            }

            this.coeff = new Complex[coeff.length - offset];
            for (int j = 0; j < this.coeff.length; j++) {
                this.coeff[j] = coeff[j];
            }
        }
    }

    /**
     * Default constructor: sets the Polynomial to the zero polynomial.
     */
    public Polynomial() {

        this.coeff = new Complex[] { new Complex() };
    }

    // ========================================================
    // Operations and functions with polynomials.
    // ========================================================

    /**
     * Return the coefficients array.
     *
     * @return The coefficients array.
     */
    public Complex[] getCoeff() {

        return this.coeff;
    }

    /**
     * Create a string representation of the polynomial.
     * Use z to represent the variable. I included terms
     * with zero co-efficients up to the degree of the
     * polynomial.
     *
     * For example: (-5.000+5.000i) + (2.000-2.000i)z + (-1.000+0.000i)z^2
     */
    public String toString() {

        String repr = "";
        for (int i = 0; i < this.coeff.length; i++) {
            if (i == 0) {
                repr += String.format("(%s)", this.coeff[i].toString());
            } else if (i == 1) {
                repr += String.format(" + (%s)z", this.coeff[i].toString());
            } else {
                repr += String.format(" + (%s)z^%d", this.coeff[i].toString(), i);
            }
        }
        return repr;
    }

    /**
     * Returns the degree of this polynomial.
     */
    public int degree() {

        return this.coeff.length - 1;
    }

    /**
     * Evaluates the polynomial at a given point z.
     *
     * @param z The point at which to evaluate the polynomial
     * @return The complex number P(z).
     */
    public Complex evaluate(Complex z) {

        // Horner's method
        Complex eval = new Complex();
        for (int i = this.coeff.length - 1; i >= 0; i--) {
            eval = this.coeff[i].add(eval.multiply(z));
        }
        return eval;
    }

    // ========================================================
    // Tester function.
    // ========================================================

}