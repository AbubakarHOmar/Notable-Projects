#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>
#include <mkl_lapacke.h>

/* Define the structure for a banded matrix */
typedef struct
{
    long columns;     /* Total columns in the matrix */
    long band_rows;   /* Rows in banded form */
    long upper_bands; /* Bands above the main diagonal */
    long lower_bands; /* Bands below the main diagonal */
    double *data;     /* Banded matrix data */

    /* Temporary storage for matrix inversion */
    long inverse_rows;    /* Rows in the inverted matrix */
    double *inverse_data; /* Inverted matrix data */
    int *pivot_indices;   /* Pivot indices for inversion */
} BandedMatrix;

/* Initialize a banded matrix with specified dimensions and band widths. */
int setup_banded_matrix(BandedMatrix *matrix, long lower_band_count, long upper_band_count, long column_count)
{
    if (lower_band_count < 0 || upper_band_count < 0 || column_count <= 0)
    {
        return 0; // Validation failed
    }

    matrix->columns = column_count;
    matrix->band_rows = lower_band_count + upper_band_count + 1;
    matrix->upper_bands = upper_band_count;
    matrix->lower_bands = lower_band_count;
    matrix->inverse_rows = upper_band_count * 2 + lower_band_count + 1;

    // Allocate memory for matrix data and inversion-related data
    matrix->data = (double *)calloc(matrix->band_rows * matrix->columns, sizeof(double));
    matrix->inverse_data = (double *)calloc((matrix->band_rows + matrix->lower_bands) * matrix->columns, sizeof(double));
    matrix->pivot_indices = (int *)malloc(matrix->columns * sizeof(int));

    // Check for memory allocation errors
    if (!matrix->data || !matrix->inverse_data || !matrix->pivot_indices)
    {
        free(matrix->data);
        free(matrix->inverse_data);
        free(matrix->pivot_indices);
        return 0;
    }
    return 1;
}

/* Clean up resources used by a banded matrix. */
void cleanup_banded_matrix(BandedMatrix *matrix)
{
    free(matrix->data);
    free(matrix->inverse_data);
    free(matrix->pivot_indices);

    matrix->data = NULL;
    matrix->inverse_data = NULL;
    matrix->pivot_indices = NULL;
}

/* Retrieves a reference to a specific element in the banded matrix,
 * given the row and column indices from the full matrix perspective. */
double *element_pointer(BandedMatrix *matrix, long i, long j)
{
    int band_index = matrix->upper_bands + i - j;
    if (i < 0 || j < 0 || i >= matrix->columns || j >= matrix->columns)
    {
        printf("Error: Index out of bounds [%ld, %ld] for columns %ld\n", i, j, matrix->columns);
        exit(EXIT_FAILURE);
    }
    return &(matrix->data[matrix->band_rows * j + band_index]);
}

/* Retrieves the value at a given row and column in the full matrix from the banded storage. */
double fetch_value(BandedMatrix *matrix, long i, long j)
{
    return *element_pointer(matrix, i, j);
}

/* Assigns a value to a specified location in the banded matrix, using full matrix indices. */
void assign_value(BandedMatrix *matrix, long i, long j, double value)
{
    *element_pointer(matrix, i, j) = value;
}

/* Solves the system of linear equations Ax = b for x, where A is a banded matrix. */
int solve_linear_system(BandedMatrix *matrix, double *solution, double *rhs)
{
    int status, band_index;
    // Prepare LAPACK input by copying data to inverse_data array
    for (int col = 0; col < matrix->columns; col++)
    {
        for (band_index = 0; band_index < matrix->band_rows; band_index++)
        {
            matrix->inverse_data[matrix->inverse_rows * col + (band_index + matrix->lower_bands)] =
                matrix->data[matrix->band_rows * col + band_index];
        }
        solution[col] = rhs[col];
    }

    long num_rhs = 1; // Number of right-hand sides, i.e., number of b vectors
    long ldb = matrix->lower_bands * 2 + matrix->upper_bands + 1;
    status = LAPACKE_dgbsv(LAPACK_COL_MAJOR, matrix->columns, matrix->lower_bands, matrix->upper_bands, num_rhs,
                           matrix->inverse_data, ldb, matrix->pivot_indices, solution, matrix->columns);
    return status;
}

/* Determines the two-dimensional grid position from a one-dimensional array index. */
void calculate_position(long index, long rows_total, long *col, long *row)
{
    *row = index % rows_total;
    *col = (index - *row) / rows_total;
}

/* Function to exchange the values of two pointers to double. */
void swapDoublePointers(double **ptr1, double **ptr2)
{
    double *temp = *ptr1; // Hold the first pointer's address temporarily
    *ptr1 = *ptr2;        // Assign second pointer's address to first pointer
    *ptr2 = temp;         // Assign stored address to second pointer
}

/* Outputs a vector's elements and associated position and time data to a specified file. */
int outputVectorData(FILE *outputFile, double **dataVector, int vectorSize, long **positionArray, int yPosCount, double currentTime)
{
    if (!outputFile)
    { // Verify the file pointer is valid
        fprintf(stderr, "Failed to open the output file.\n");
        return 1; // Indicate error
    }

    for (int i = 0; i < vectorSize; i++)
    {
        // Derive position coordinates based on the position array
        long xPosition = (*positionArray)[i] / yPosCount;
        long yPosition = (*positionArray)[i] % yPosCount;

        // Write the current time, position, and vector element to the file
        fprintf(outputFile, "%.6f, %ld, %ld, %.6f\n", currentTime, xPosition, yPosition, (*dataVector)[i]);
    }

    fprintf(outputFile, "\n"); // Ensure there's a newline at the end of the output
    return 0;                  // Successful completion
}

// Structure for handling banded matrices omitted for brevity
// Assume band_matrix and corresponding functions such as setup_banded_matrix are defined elsewhere

int main()
{
    // Attempt to open the input file for reading simulation parameters
    FILE *inputFile = fopen("input.txt", "r");
    if (!inputFile)
    {
        perror("Failed to open input file");
        return EXIT_FAILURE;
    }

    int gridX, gridY, activeCells;
    double domainLengthX, domainLengthY, endTime, lambdaParam, diagnosticStep;

    // Attempt to read simulation parameters from the file
    if (fscanf(inputFile, "%d %d %d %lf %lf %lf %lf %lf", &gridX, &gridY, &activeCells, &domainLengthX, &domainLengthY, &endTime, &lambdaParam, &diagnosticStep) != 8)
    {
        fprintf(stderr, "Failed to read simulation parameters\n");
        fclose(inputFile);
        return EXIT_FAILURE;
    }

    // Properly close the input file after reading
    fclose(inputFile);

    // Initialize the banded matrix with appropriate dimensions
    BandedMatrix matrix;
    setup_banded_matrix(&matrix, gridY, gridY, activeCells);

    // Allocate memory for vectors used in the linear equation Ax = b
    double *initialVector = calloc(activeCells, sizeof(double));
    if (!initialVector)
    {
        fprintf(stderr, "Memory allocation failed for initial vector\n");
        return EXIT_FAILURE;
    }

    double *nextVector = calloc(activeCells, sizeof(double));
    if (!nextVector)
    {
        fprintf(stderr, "Memory allocation failed for next vector\n");
        free(initialVector);
        return EXIT_FAILURE;
    }

    // Compute grid spacings
    double deltaX = domainLengthX / gridX;
    double deltaY = domainLengthY / gridY;

    // Allocate and initialize index arrays
    long *cellIndex = calloc(activeCells, sizeof(long));
    long *equationIndex = calloc(gridY * gridY, sizeof(long));
    if (!cellIndex || !equationIndex)
    {
        fprintf(stderr, "Memory allocation failed for index arrays\n");
        free(initialVector);
        free(nextVector);
        free(cellIndex);
        free(equationIndex);
        return EXIT_FAILURE;
    }

    for (int i = 0; i < gridY * gridY; i++)
    {
        equationIndex[i] = -1; // Mark all equations as unassigned initially
    }

    // Determine the maximum potential value as a scaling factor
    double maxPotential = sqrt(fabs(lambdaParam));

    // Open coefficients file to read matrix coefficients
    FILE *coeffFile = fopen("coefficients.txt", "r");
    if (!coeffFile)
    {
        perror("Failed to open coefficients file");
        free(cellIndex);
        free(equationIndex);
        free(initialVector);
        free(nextVector);
        return EXIT_FAILURE;
    }

    for (int i = 0; i < activeCells; i++)
    {
        int xCoord, yCoord;
        if (fscanf(coeffFile, "%d %d %lf", &xCoord, &yCoord, &nextVector[i]) != 3)
        {
            fprintf(stderr, "Failed to read coefficients\n");
            fclose(coeffFile);
            free(cellIndex);
            free(equationIndex);
            free(initialVector);
            free(nextVector);
            return EXIT_FAILURE;
        }

        // Adjust the maximum potential if necessary
        if (fabs(nextVector[i]) > maxPotential)
        {
            maxPotential = fabs(nextVector[i]);
        }

        // Compute and store the linear index for the matrix
        cellIndex[i] = xCoord * gridY + yCoord;

        // Map the cell to its equation in the matrix
        equationIndex[cellIndex[i]] = i;
    }

    // Clean up and close the coefficients file
    fclose(coeffFile);

    // Adjusting lambda's impact based on its sign
    int lambdaAdjustment = lambdaParam < 0 ? -1 : 1;

    // Simplifying boundary calculations into a unified expression
    double boundaryLimit1 = 0.5 * pow(deltaX, 2) * pow(deltaY, 2) / (pow(deltaX, 2) + pow(deltaY, 2));
    double boundaryLimit2 = maxPotential > 0 && maxPotential > sqrt(lambdaParam * lambdaAdjustment) ? 1 / (maxPotential * (sqrt(lambdaParam * lambdaAdjustment) + maxPotential)) : 1 / (2 * lambdaParam * lambdaAdjustment);

    // Directly determining the lower of the two calculated boundaries
    double lowerBoundary = fmin(boundaryLimit1, boundaryLimit2);

    // Setting initial timestep based on diagnostic interval
    double timeStep = diagnosticStep;
    int divisionFactor = 1;

    // Refining timestep to stay within boundary constraints
    while (timeStep > lowerBoundary)
    {
        divisionFactor++;
        timeStep = diagnosticStep / divisionFactor;
    }

    // Processing each active cell within the grid
    bool hasTopNeighbor;
    bool hasBottomNeighbor;
    bool hasLeftNeighbor;
    bool hasRightNeighbor;

    for (int cellIdx = 0; cellIdx < activeCells; cellIdx++)
    {
        // Ensuring diagonal dominance
        assign_value(&matrix, cellIdx, cellIdx, 1);

        // Identifying and handling neighboring active grid points
        if (cellIndex[cellIdx] % gridY == gridY - 1)
        {
            hasTopNeighbor = false;
        }
        else if ((equationIndex[cellIndex[cellIdx] + 1] == -1))
        {
            hasTopNeighbor = false;
        }
        else
        {
            hasTopNeighbor = true;
        }

        if (cellIndex[cellIdx] % gridY == 0)
        {
            hasBottomNeighbor = false;
        }
        else if ((equationIndex[cellIndex[cellIdx] - 1] == -1))
        {
            hasBottomNeighbor = false;
        }
        else
        {
            hasBottomNeighbor = true;
        }

        if (cellIndex[cellIdx] < gridY)
        {
            hasLeftNeighbor = false;
        }
        else if ((equationIndex[cellIndex[cellIdx] - gridY] == -1))
        {
            hasLeftNeighbor = false;
        }
        else
        {
            hasLeftNeighbor = true;
        }

        if (cellIndex[cellIdx] >= (gridX - 1) * gridY)
        {
            hasRightNeighbor = false;
        }
        else if ((equationIndex[cellIndex[cellIdx] + gridY] == -1))
        {
            hasRightNeighbor = false;
        }
        else
        {
            hasRightNeighbor = true;
        }

        // Adjusting matrix entries based on neighbors and boundary conditions
        if (hasBottomNeighbor)
        {
            assign_value(&matrix, cellIdx, cellIdx - 1, -(timeStep) / (pow(deltaY, 2)));
            assign_value(&matrix, cellIdx, cellIdx, fetch_value(&matrix, cellIdx, cellIdx) + (timeStep) / (pow(deltaY, 2)));
        }
        if (hasTopNeighbor)
        {
            assign_value(&matrix, cellIdx, cellIdx + 1, -(timeStep) / (pow(deltaY, 2)));
            assign_value(&matrix, cellIdx, cellIdx, fetch_value(&matrix, cellIdx, cellIdx) + (timeStep) / (pow(deltaY, 2)));
        }
        if (hasRightNeighbor)
        {
            assign_value(&matrix, cellIdx, equationIndex[cellIndex[cellIdx] + gridY], -(timeStep) / (pow(deltaX, 2)));
            assign_value(&matrix, cellIdx, cellIdx, fetch_value(&matrix, cellIdx, cellIdx) + (timeStep) / (pow(deltaX, 2)));
        }
        if (hasLeftNeighbor)
        {
            assign_value(&matrix, cellIdx, equationIndex[cellIndex[cellIdx] - gridY], -(timeStep) / (pow(deltaX, 2)));
            assign_value(&matrix, cellIdx, cellIdx, fetch_value(&matrix, cellIdx, cellIdx) + (timeStep) / (pow(deltaX, 2)));
        }
    }

    // Releasing allocated memory for equation indexes
    free(equationIndex);

    // Preparing to document the simulation's initial state
    FILE *outputStream = fopen("output.txt", "w");

    // Documenting the initial vector state
    outputVectorData(outputStream, &nextVector, activeCells, &cellIndex, gridY, 0.0);

    // Establishing iteration count based on the simulation timeframe and timestep
    int iterations = floor(endTime / timeStep);

    // Executing time-stepping iterations
    for (int step = 1; step <= iterations; step++)
    {
        // Applying explicit updates to the solution vector
        for (int j = 0; j < activeCells; j++)
        {
            nextVector[j] = (1 + timeStep * lambdaParam) * nextVector[j] - timeStep * pow(nextVector[j], 3);
        }

        // Resolving the linear system with the updated right-hand side
        solve_linear_system(&matrix, initialVector, nextVector);

        // Exchanging the solution and the auxiliary vectors for the next iteration
        swapDoublePointers(&initialVector, &nextVector);

        // Periodically outputting the solution state
        if (step % divisionFactor == 0)
        {
            outputVectorData(outputStream, &nextVector, activeCells, &cellIndex, gridY, step * timeStep);
        }
    }

    // Finalizing documentation and releasing resources
    fclose(outputStream);

    // Cleaning up allocated structures and memory
    cleanup_banded_matrix(&matrix);
    free(initialVector);
    free(nextVector);
    free(cellIndex);

    return 0;
}