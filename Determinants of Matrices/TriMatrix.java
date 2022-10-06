/*
 * PROJECT III: TriMatrix.java
 *
 * This file contains a template for the class TriMatrix. Not all methods are
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

public class TriMatrix extends Matrix {
    /**
     * An array holding the diagonal elements of the matrix.
     */
    private double[] diagonal;

    /**
     * An array holding the upper-diagonal elements of the matrix.
     */
    private double[] upperDiagonal;

    /**
     * An array holding the lower-diagonal elements of the matrix.
     */
    private double[] lowerDiagonal;
    
    /**
     * Constructor function: should initialise iDim and jDim through the Matrix
     * constructor and set up the values array.
     *
     * @param dimension  The dimension of the array.
     */
    public TriMatrix(int dimension) {
        // You need to fill in this method.
        super(dimension, dimension);
        diagonal = new double[iDim];
        upperDiagonal = new double[iDim - 1];
        lowerDiagonal = new double[iDim - 1];
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
        if ((i < 0 || i >= iDim) || (j < 0 || j >= iDim)) {
            throw new MatrixException("Entry doesn't exist!");
        }
        if (i == j) {
            return diagonal[i];
        }
        else if (j == i + 1) {
            return upperDiagonal[i];
        }
        else if (j == i - 1) {
            return lowerDiagonal[j];
        }
        else {
            return 0.0;
        }
    }
    
    /**
     * Setter function: set the (i,j)'th entry of the data array.
     *
     * @param i      The location in the first co-ordinate.
     * @param j      The location in the second co-ordinate.
     * @param value  The value to set the (i,j)'th entry to.
     */
    public void setIJ(int i, int j, double value) {
        // You need to fill in this method.
        if ((i < 0 || i >= iDim) || (j < 0 || j >= iDim)) {
            throw new MatrixException("Entry doesn't exist!");
        }
        if (i == j) {
            diagonal[i] = value;
        }
        else if (j == i + 1) {
            upperDiagonal[i] = value;
        }
        else if (j == i - 1) {
            lowerDiagonal[j] = value;
        }
        else {
            throw new MatrixException(":(");
        }
}
    
    /**
     * Return the determinant of this matrix.
     *
     * @return The determinant of the matrix.
     */
    public double determinant() {
        // You need to fill in this method.
        double det = 1.0;
        TriMatrix LU = LUdecomp();
        for (int i = 0; i < LU.iDim; i++) {
            det *= LU.diagonal[i];
        }
        return det;
    }
    
    /**
     * Returns the LU decomposition of this matrix. See the formulation for a
     * more detailed description.
     * 
     * @return The LU decomposition of this matrix.
     */
    public TriMatrix LUdecomp() {
        // You need to fill in this method.
        TriMatrix LU = new TriMatrix(iDim);
        LU.diagonal[0] = diagonal[0];
        LU.lowerDiagonal[0] = 0;
        for (int i = 1; i < iDim; i++) {
            LU.lowerDiagonal[i - 1] = lowerDiagonal[i - 1] / LU.diagonal[i - 1];
            LU.diagonal[i] = diagonal[i] - (LU.lowerDiagonal[i - 1] * upperDiagonal[i - 1]);
            LU.upperDiagonal[i - 1] = upperDiagonal[i - 1];
    } 
        return LU;
    }

    /**
     * Add the matrix to another second matrix.
     *
     * @param second  The Matrix to add to this matrix.
     * @return        The sum of this matrix with the second matrix.
     */
    public Matrix add(Matrix second) {
        // You need to fill in this method.
        if (iDim != second.iDim || jDim != second.jDim) {
            throw new MatrixException("The two Matrices don't have the same dimension!");
        }
        GeneralMatrix ret = new GeneralMatrix(iDim, jDim);
        for (int i = 0; i < iDim; i++) {
            for (int j = 0; j < jDim; j++) {
                ret.setIJ(i, j, getIJ(i, j) + second.getIJ(i, j));
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
                    ret.setIJ(i, j, ret.getIJ(i, j) + (getIJ(i, k) * A.getIJ(k, j)));
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
                ret.setIJ(i, j, getIJ(i, j) * scalar);
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
        diagonal[0] = rand.nextDouble();
        for (int i = 1; i < iDim; i++) {
            lowerDiagonal[i - 1] = rand.nextDouble();
            diagonal[i] = rand.nextDouble();
            upperDiagonal[i - 1] = rand.nextDouble();
        }
    }
    
    /*
     * Your tester function should go here.
     */
    public static void main(String[] args) {
        //Test your class implementation using this method.
        TriMatrix A = new TriMatrix(3);
        TriMatrix B = new TriMatrix(3);

        A.random();
        B.random();

        System.out.println("A:\n" + A.toString());
        System.out.println("B:\n" + B.toString());

        System.out.println("LU decomp of A:\n" + A.LUdecomp());
        System.out.println("LU decomp of B:\n" + B.LUdecomp());

        System.out.println("3A + 4B:\n" + A.multiply(3).add(B.multiply(4)).toString());
        System.out.println("AB:\n" + A.multiply(B).toString());

        System.out.println("det(A): " + A.determinant());
        System.out.println("det(B): " + B.determinant());

    }

}