

/*
 * In this class, I create a basic Java object responsible for
 * performing the Secant root finding method on a given polynomial
 * f(z) with complex co-efficients.
 */

public class Secant {
    /**
     * The maximum number of iterations that should be used when applying
     * Secant.
     */
    public static final int MAXITER = 20;

    /**
     * The tolerance that should be used throughout this project.
     */
    public static final double TOL = 1.0e-10;

    /**
     * The polynomial we wish to apply the Secant method to.
     */
    private Polynomial f;


    /**
     * A root of the polynomial f corresponding to the root found by the
     * iterate() function below.
     */
    private Complex root;
    
    /**
     * The number of iterations required to reach within TOL of the root.
     */
    private int numIterations;

    /**
     * An enumeration that signifies errors that may occur in the root finding
     * process.
     *
     * Possible values are:
     *   OK: Nothing went wrong.
     *   ZERO: Difference went to zero during the algorithm.
     *   DNF: Reached MAXITER iterations (did not finish)
     */
    enum Error { OK, ZERO, DNF };
    private Error err = Error.OK;
    
    
    // ========================================================
    // Constructor functions.
    // ========================================================

    /**
     * Basic constructor.
     *
     * @param p  The polynomial used for Secant.
     */
    public Secant(Polynomial p) {
        
        this.f = p;
        this.root = new Complex();
    }

    // ========================================================
    // Accessor methods.
    // ========================================================
    
    /**
     * Returns the current value of the err instance variable.
     */
    public Error getError() {
        
        return err;
    }

    /**
     * Returns the current value of the numIterations instance variable.
     */
    public int getNumIterations() { 
        
        return this.numIterations;
    }
    
    /**
     * Returns the current value of the root instance variable.
     */
    public Complex getRoot() {
        
        return new Complex(this.root.getReal(), this.root.getImag());
    }

    /**
     * Returns the polynomial associated with this object.
     */
    public Polynomial getF() {
        
        return new Polynomial(this.f.getCoeff());
    }

    // ========================================================
    // Secant method
    // ========================================================
    
    /**
     * Given two complex numbers z0 and z1, apply Secant to the polynomial f in
     * order to find a root within tolerance TOL.
     *
     * One of three things may occur:
     *
     *   - The root is found, in which case, I set root to the end result of the
     *     algorithm, numIterations to the number of iterations required to
     *     reach it and err to 0.
     *   - At some point the absolute difference between f(zn) and f(zn-1) becomes zero. 
     *     In this case, I set err to -1 and return.
     *   - After MAXITER iterations the algorithm has not converged. In this 
     *     case I set err to -2 and return.
     *
     * @param z0,z1  The initial starting points for the algorithm.
     */
    public void iterate(Complex z0, Complex z1) {
        
        for (int i = 1; i <= MAXITER; i++) {
            Complex z2 = z1.add(f.evaluate(z1).multiply(z1.add(z0.negate()).divide(f.evaluate(z1).add(f.evaluate(z0).negate()))).negate());

            if (z2.add(z1.negate()).abs() < Secant.TOL && f.evaluate(z2).abs() < Secant.TOL) {
                this.root = z2;
                this.numIterations = i;
                err = Secant.Error.OK;
                return;  
            }
            else if ((f.evaluate(z2)).add((f.evaluate(z1).negate())).abs() < Secant.TOL) {
                err = Secant.Error.ZERO;
                return;
              }
              
            z0 = z1;
            z1 = z2;
        }
        err = Secant.Error.DNF;
    }
      
    // ========================================================
    // Tester function.
    // ========================================================
    
    public static void main(String[] args) {
        // Basic tester: find a root of f(z) = z^3-1.
        Complex[] coeff = new Complex[] { new Complex(-1.0,0.0), new Complex(), new Complex(), new Complex(1.0,0.0) };
        Polynomial p    = new Polynomial(coeff);
        Secant     s    = new Secant(p);
                
        s.iterate(new Complex(), new Complex(1.0,1.0));
        System.out.println(p.toString());
        System.out.println(s.getNumIterations());   // 12
        System.out.println(s.getError());
        System.out.println(s.getRoot());
    }
}
