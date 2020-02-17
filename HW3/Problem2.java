import java.util.concurrent.*;
import java.util.Arrays;
import java.util.Scanner;
class matmult
{
    private float[][] a, b;
    private float[][] c;
    private int m, n, p;

    // A (m x n) B (n x p) = C (m x p)
    public matmult( float A[][], float B[][], float C[][], int m, int n, int p) {
        this.a = A;
        this.b = B;
        this.c = C;
        this.m = m;
        this.n = n;
        this.p = p;
    }

    public void matMult(int threadCount) {
        c = new float[c.length][c[0].length];
        CountDownLatch latch = new CountDownLatch(m * p);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        long timeStart = System.currentTimeMillis();
        for (int x = 0; x < m; x++) {
            for (int y = 0; y < p; y++) {
                int row = x;
                int col = y;
                pool.submit(() -> {
                    for (int i = 0; i < this.a[0].length; i++) {
                        this.c[row][col] += this.a[row][i] * this.b[i][col];
                    }
                    latch.countDown();
                });
            }
        }
        pool.shutdown();
        try {
            latch.await();
            System.out.println(
                    "Time elapsed for " + threadCount + " threads: " + (System.currentTimeMillis() - timeStart) + "ms");
        } catch (final InterruptedException e) {
            System.out.println("Timed Out");
            e.printStackTrace();
        }
    }
    public static float[][] fillMatrix(float a[][], int m, int n) {
        float[][] temp = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                temp[i][j] = (float)(Math.random() * (100));
            }
        }
        return temp;
    }
    public float[][] getC()
    {
        return this.c;
    }
    public static void main(final String[] args) {
        System.out.println("Starting multithreading matrix multiplication");
        int threads = 8;
        if (threads > Runtime.getRuntime().availableProcessors())
        {
            System.out.println("\tNote: There are only " + Runtime.getRuntime().availableProcessors()
                    + " physical processors in this machine");
        }
        int m = 5;
        int n = 8;
        int p = 4;
        float[][] A = new float[m][n];
        float[][] B = new float[n][p];
        float[][] C = new float[m][p];
        A = fillMatrix(A, m, n);
        B = fillMatrix(B, n, p);
        matmult matrixMult = new matmult(A, B, C, m, n, p);
        printMatrix("A", A);
        printMatrix("B", B);
        for (int i = 1; i <= threads; i++) {
            matrixMult.matMult(i);
        }
        C = matrixMult.getC();
        printMatrix("C", C);
    }

    public static void printMatrix(final String name, final float a[][]) {
        System.out.println(name + ": ");
        for (final float[] row : a) 
        {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
    }
}
