/*
 * PROJECT III: GeneralMatrix.java
 *
 * This file contains a template for the class GeneralMatrix. Not all methods
 * implemented and they do not have placeholder return statements. Make sure 
 * you have carefully read the project formulation before starting to work 
 * on this file. You will also need to have completed the Matrix class.
 *
 * Remember not to change the names, parameters or return types of any
 * variables in this file!
 *
 * The function of the methods and instance variables are outlined in the
 * comments directly above them.
 * 
 * Tasks:
 *
 * 1) Complete this class with the indicated methods and instance variables.
 *
 * 2) Fill in the following fields:
 *
 * NAME: Abubakar Omar
 * UNIVERSITY ID: 2125047   
 * DEPARTMENT: Mathematics
 */
import java.util.SplittableRandom;

public class GeneralMatrix extends Matrix {
    /**
     * This instance variable stores the elements of the matrix.
     */
    private double[][] values;

    /**
     * Constructor function: should initialise iDim and jDim through the Matrix
     * constructor and set up the data array.
     *
     * @param firstDim   The first dimension of the array.
     * @param secondDim  The second dimension of the array.
     */
    public GeneralMatrix(int firstDim, int secondDim) {
        // You need to fill in this method.
        super(firstDim, secondDim);
        this.values = new double[iDim][jDim];
    }

    /**
     * Constructor function. This is a copy constructor; it should create a
     * copy of the second matrix.
     *
     * @param second  The matrix to create a copy of.
     */
    public GeneralMatrix(GeneralMatrix second) {
        // You need to fill in this method.
        super(second.iDim, second.jDim);
        this.values = new double[iDim][jDim];
        for (int i = 0; i < second.iDim; i++) {
            for (int j = 0; j < second.jDim; j++) {
                this.setIJ(i, j, second.getIJ(i,j));
            }
        }
    }
    
    /**
     * Getter function: return the (i,j)'th entry of the matrix.
     *
     * @param i  The location in the first co-ordinate.
     * @param j  The location in the second co-ordinate.
     * @return   The (i,j)'th entry of the matrix.
     */
    public double getIJ(int i, int j) {
        // You need to fill in this method.
        if ((i < 0 || i >= values.length) || (j < 0 || j >= values[i].length)) {
            throw new MatrixException("Entry doesn't exist!");
        }
        else {
            return this.values[i][j];
        }
        
    }
    
    /**
     * Setter function: set the (i,j)'th entry of the values array.
     *
     * @param i      The location in the first co-ordinate.
     * @param j      The location in the second co-ordinate.
     * @param value  The value to set the (i,j)'th entry to.
     */
    public void setIJ(int i, int j, double value) {
        // You need to fill in this method.
        if ((i < 0 || i >= values.length) || (j < 0 || j >= values[i].length)) {
            throw new MatrixException("Entry doesn't exist!");
        }
        else {
            this.values[i][j] = value;
        }
    }
    
    /**
     * Return the determinant of this matrix.
     *
     * @return The determinant of the matrix.
     */
    public double determinant() {
        // You need to fill in this method
        double[] sign = new double[1];
        try {
            GeneralMatrix LU = LUdecomp(sign);
            double det = sign[0];
            for (int i = 0; i < LU.iDim; i++) {
                for (int j = 0; j < LU.jDim; j++) {
                    if (i == j) {
                        det *= LU.values[i][j];
                    }
                }
            }
            return det;
        }
        catch (MatrixException e) {
            String err = e.getMessage();
            if (err.equals("Matrix is singular")) {
                return 0.0;
            } else {
                throw e;
            }
        }
    }

    /**
     * Add the matrix to another second matrix.
     *
     * @param second  The Matrix to add to this matrix.
     * @return   The sum of this matrix with the second matrix.
     */
    public Matrix add(Matrix second) {
        // You need to fill in this method.
        if (iDim != second.iDim || jDim != second.jDim) {
            throw new MatrixException("The two Matrices don't have the same dimension!");
        }
        GeneralMatrix ret = new GeneralMatrix(iDim, jDim);
        for (int i = 0; i < iDim; i++) {
            for (int j = 0; j < jDim; j++) {
                ret.values[i][j] = this.values[i][j] + second.getIJ(i, j);
            }
        }
        return (Matrix)ret;
    }
    
    /**
     * Multiply the matrix by another matrix A. This is a _left_ product,
     * i.e. if this matrix is called B then it calculates the product BA.
     *
     * @param A  The Matrix to multiply by.
     * @return   The product of this matrix with the matrix A.
     */
    public Matrix multiply(Matrix A) {
        // You need to fill in this method.
        if (jDim != A.iDim) { 
            throw new MatrixException("Columns of B don't equal rows of A!");
        }
        GeneralMatrix ret = new GeneralMatrix(iDim, A.jDim);
        for (int i = 0; i < iDim; i++) {
            for (int j = 0; j < A.jDim; j++) {
                for (int k = 0; k < jDim; k++) {
                    ret.values[i][j] = ret.values[i][j] + (values[i][k] * A.getIJ(k, j));
                }
            }
        }
        return (Matrix)ret;
    }


    /**
     * Multiply the matrix by a scalar.
     *
     * @param scalar  The scalar to multiply the matrix by.
     * @return        The product of this matrix with the scalar.
     */
    public Matrix multiply(double scalar) {
        // You need to fill in this method.
        GeneralMatrix ret = new GeneralMatrix(iDim, jDim);
        for (int i = 0; i < iDim; i++) {
            for (int j = 0; j < jDim; j++) {
                ret.values[i][j] = this.values[i][j] * scalar;
            }
        }
        return (Matrix)ret;
    }

    /**
     * Populates the matrix with random numbers which are uniformly
     * distributed between 0 and 1.
     */
    public void random() {
        // You need to fill in this method.
        SplittableRandom rand = new SplittableRandom();
        for (int i = 0; i < iDim; i++) {
            for (int j = 0; j < jDim; j++) {
                this.values[i][j] = rand.nextDouble();
            }
        }
    }

    /**
     * Returns the LU decomposition of this matrix; i.e. two matrices L and U
     * so that A = LU, where L is lower-diagonal and U is upper-diagonal.
     * 
     * On exit, decomp returns the two matrices in a single matrix by packing
     * both matrices as follows:
     *
     * [ u_11 u_12 u_13 u_14 ]
     * [ l_21 u_22 u_23 u_24 ]
     * [ l_31 l_32 u_33 u_34 ]
     * [ l_41 l_42 l_43 u_44 ]
     *
     * where u_ij are the elements of U and l_ij are the elements of l. When
     * calculating the determinant you will need to multiply by the value of
     * sign[0] calculated by the function.
     * 
     * If the matrix is singular, then the routine throws a MatrixException.
     * In this case the string from the exception's getMessage() will contain
     * "singular"
     *
     * This method is an adaptation of the one found in the book "Numerical
     * Recipies in C" (see online for more details).
     * 
     * @param sign  An array of length 1. On exit, the value contained in here
     *              will either be 1 or -1, which you can use to calculate the
     *              correct sign on the determinant.
     * @return      The LU decomposition of the matrix.
     */
public GeneralMatrix LUdecomp(double[] sign) {
        // This method is complete. You should not even attempt to change it!!
        if (jDim != iDim)
            throw new MatrixException("Matrix is not square");
        if (sign.length != 1)
            throw new MatrixException("d should be of length 1");
        
        int           i, imax = -10, j, k; 
        double        big, dum, sum, temp;
        double[]      vv   = new double[jDim];
        GeneralMatrix a    = new GeneralMatrix(this);
        
        sign[0] = 1.0;
        
        for (i = 1; i <= jDim; i++) {
            big = 0.0;
            for (j = 1; j <= jDim; j++)
                if ((temp = Math.abs(a.values[i-1][j-1])) > big)
                    big = temp;
            if (big == 0.0)
                throw new MatrixException("Matrix is singular");
            vv[i-1] = 1.0/big;
        }
        
        for (j = 1; j <= jDim; j++) {
            for (i = 1; i < j; i++) {
                sum = a.values[i-1][j-1];
                for (k = 1; k < i; k++)
                    sum -= a.values[i-1][k-1]*a.values[k-1][j-1];
                a.values[i-1][j-1] = sum;
            }
            big = 0.0;
            for (i = j; i <= jDim; i++) {
                sum = a.values[i-1][j-1];
                for (k = 1; k < j; k++)
                    sum -= a.values[i-1][k-1]*a.values[k-1][j-1];
                a.values[i-1][j-1] = sum;
                if ((dum = vv[i-1]*Math.abs(sum)) >= big) {
                    big  = dum;
                    imax = i;
                }
            }
            if (j != imax) {
                for (k = 1; k <= jDim; k++) {
                    dum = a.values[imax-1][k-1];
                    a.values[imax-1][k-1] = a.values[j-1][k-1];
                    a.values[j-1][k-1] = dum;
                }
                sign[0] = -sign[0];
                vv[imax-1] = vv[j-1];
            }
            if (a.values[j-1][j-1] == 0.0)
                a.values[j-1][j-1] = 1.0e-20;
            if (j != jDim) {
                dum = 1.0/a.values[j-1][j-1];
                for (i = j+1; i <= jDim; i++)
                    a.values[i-1][j-1] *= dum;
            }
        }
        
        return a;
    }

    /*
     * Your tester function should go here.
     */
    public static void main(String[] args) {
        // Test your class implementation using this method.
        GeneralMatrix A = new GeneralMatrix(2, 2);
        GeneralMatrix B = new GeneralMatrix(2, 2);

        A.random();
        B.random();

        System.out.println("A:\n" + A.toString());
        System.out.println("B:\n" + B.toString());

        System.out.println("3A + 4B:\n" + A.multiply(3).add(B.multiply(4)).toString());
        System.out.println("AB:\n" + A.multiply(B).toString());

        System.out.println("A:\n" + A.toString());

        double[] sign = new double[1];
        System.out.println("LU decomp of A:\n" + A.LUdecomp(sign));

        System.out.println("A:\n" + A.toString());

        System.out.println("LU decomp of B:\n" + B.LUdecomp(sign));
        
        System.out.println("det(A): " + A.determinant());
        System.out.println("det(B): " + B.determinant());
        
    }
}