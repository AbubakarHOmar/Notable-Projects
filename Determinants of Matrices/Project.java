//This file contains a template for the class Project. 

public class Project {
    /**
     * Calculates the variance of the distribution defined by the determinant
     * of a random matrix. See the formulation for a detailed description.
     *
     * @param matrix      The matrix object that will be filled with random
     *                    samples.
     * @param nSamp       The number of samples to generate when calculating 
     *                    the variance. 
     * @return            The variance of the distribution.
     */
    public static double matVariance(Matrix matrix, int nSamp) {
        
        double mean = 0.0;
        double square_of_mean = 0.0;
        for (int i = 1; i <= nSamp; i++) {
            matrix.random();
            double det = matrix.determinant();
            mean += det;
            square_of_mean += det * det;
        }
        mean /= nSamp;
        square_of_mean /= nSamp;
        return (square_of_mean - (mean * mean));
    }
    
    /**
     * This function calculates the variances of matrices for matrices
     * of size 2 <= n <= 50 and prints the results to the output.
     */
    public static void main(String[] args) {
        
        for (int n = 2; n <= 50; n++) {
            GeneralMatrix general_test = new GeneralMatrix(n, n);
            TriMatrix tri_test = new TriMatrix(n);
            System.out.println(String.format("%d", n) + " " + String.format("%.15E", matVariance(general_test, 20000)) + " " + String.format("%.15E", matVariance(tri_test, 200000)));           
        }   
    }
}