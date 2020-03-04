import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
class match implements Callable<Fighter1> {
    Fighter1 F1, F2;
    Future<Fighter1> a,b;
    public match(Fighter1 a, Fighter1 b)
    {
        this.F1 = a;
        this.F2 = b;
    }
    public match(Future<Fighter1> F1, Future<Fighter1> F2)
    {
        a = F1;
        b = F2;
    }
    public Fighter1 getResult()
    {
        return call();
    }
    public Fighter1 call()
    {
        try
        {
            if(this.a != null)
            {
                this.F1 = this.a.get();
                if(this.b == null)
                    this.F2 = null;
                else  
                    this.F2 = b.get();
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        if(this.F2 == null)
            return this.F1;

        F1.score = F1.match(F1, F2);
        Fighter1 winner = F1.score >= 0?F1:F2;
        winner.reroll();
        return winner;
    }
}
class Fighter1 implements Callable<Integer>
{
    int RPS; // 0 = rock, 1 = paper, 2 = scissors
    int score, FighterID;
    CountDownLatch finLatch;
    List<Fighter1> opponents;
    String name;

    public Fighter1(int FighterID, String name)
    {
        this.FighterID = FighterID;
        this.name = name;
        System.out.println(LocalTime.now() + ":  The Fighter: " + name + " has been created");
    }
    public void init()
    {
        this.RPS = new Random().nextInt(3);
    }
    public void reroll()
    {
        this.RPS = new Random().nextInt(3);
    }
     public Integer match(Fighter1 currentFighter, Fighter1 oppennetFighter)
    {
        Integer Score = 0;
        while (Score == 0)
        {
            if(currentFighter.RPS == oppennetFighter.RPS)
            {
                System.out.println(LocalTime.now() + ":  "+ currentFighter.name + " has TIED with " + oppennetFighter.name);
                Score = 0;
            }
            if((currentFighter.RPS == 0 && oppennetFighter.RPS == 2) || (currentFighter.RPS == 1 && oppennetFighter.RPS == 0) || (currentFighter.RPS == 2 && oppennetFighter.RPS == 1))
            {
                if(currentFighter.RPS == 0)
                    System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has WON with Rock, against " + oppennetFighter.name);
                else if(currentFighter.RPS == 1)
                    System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has WON with Paper, against " + oppennetFighter.name);
                else
                    System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has WON with Scissors, against " + oppennetFighter.name);
                Score = 1;
            }
            else
            {
                System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has LOST to " + oppennetFighter.name);
                Score = -1;
            }
        }
        return Score;
    }
    public Integer call()
    {
        int i = new Random().nextInt(opponents.size()-1);
        if(FighterID!=i)
        {
            score = match(this, opponents.get(i));
            finLatch.countDown();
        }
        return score;
    }
}
public class RPSV1
{
    public RPSV1(int numFighters, int numThreads)
    {
        BlockingQueue<match> fights = new ArrayBlockingQueue<>(numFighters);
        try
        {
            ExecutorService pool = Executors.newFixedThreadPool(numThreads);

            for(int i =0; i < numFighters; i += 2)
            {
                fights.put(new match(new Fighter1(i, "Fighter " + i), new Fighter1((i+1), "Fighter " + (i+1))));
            }
            while(fights.size() > 1)
            {
                try
                {
                    fights.add(new match(pool.submit(fights.poll()).get(), pool.submit(fights.poll()).get()));
                }
                catch(InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
            System.out.println(pool.submit(fights.poll()).get().name + " wins");
            pool.shutdown();
        }
        catch(InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) 
    {
        System.out.print("Enter the number of Fighters:  ");
        int numFighters = new Scanner(System.in).nextInt();
        System.out.println("Starting Match...");
        long StartTime = System.currentTimeMillis();
        new RPSV1(numFighters, Runtime.getRuntime().availableProcessors());

        System.out.println("The Match took " + (System.currentTimeMillis()-StartTime) + "ms");
    }
}