public class singleThread
{
    public void run(int num)
    {
        int count = 0;
        int[] numbers = new int[num-2];
        for(int i =0; i < numbers.length; i++)
            numbers[i] = i+2;
        for(int i = 0; i < numbers.length; i++)
        {
            int prime =numbers[i]; // grab the first value in the array will forsure be prime
            if (prime == -1)
                continue;
            if(count == 9)
            {
                System.out.println();
                count = 0;
            }
            System.out.print(prime + "\t");
            count++;
            for(int j =i + 1; j < numbers.length; j++)
            {
                if(numbers[j] % prime == 0)
                    numbers[j] = -1;
            }
        }
    }
    public static void main(String[] args) {
        singleThread sT = new singleThread();
        long sTime = System.currentTimeMillis();
        sT.run(1000000); 
        System.out.println("\nThe total Time elapsed: " + (System.currentTimeMillis() - sTime)/1000F + " seconds");
    }
}