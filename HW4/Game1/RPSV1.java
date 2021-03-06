import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.Random;
import java.util.ArrayList;
import java.time.LocalTime;

class Fighter implements Callable<Integer>
{
    int RPS; // 0 = rock, 1 = paper, 2 = scissors
    int score, FighterID;
    CountDownLatch finLatch;
    List<Fighter> opponents;
    String name;

    public Fighter(int FighterID, CountDownLatch fLatch, CountDownLatch cLatch, List<Fighter> Fighters, String name)
    {
        this.FighterID = FighterID;
        this.finLatch = cLatch;
        this.opponents = Fighters;
        this.name = name;
        System.out.println(LocalTime.now() + ":  The Fighter: " + name + " has been created");
        fLatch.countDown();
    }
    public void init()
    {
        this.RPS = new Random().nextInt(3);
    }
    public Integer match(Fighter currentFighter, Fighter oppennetFighter)
    {
        if(currentFighter.RPS == oppennetFighter.RPS)
        {
            System.out.println(LocalTime.now() + ":  "+ currentFighter.name + " has TIED with " + oppennetFighter.name);
            return 0;
        }
        if((currentFighter.RPS == 0 && oppennetFighter.RPS == 2) || (currentFighter.RPS == 1 && oppennetFighter.RPS == 0) || (currentFighter.RPS == 2 && oppennetFighter.RPS == 1))
        {
            if(currentFighter.RPS == 0)
                System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has WON with Rock, against " + oppennetFighter.name);
            else if(currentFighter.RPS == 1)
                System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has WON with Paper, against " + oppennetFighter.name);
            else
                System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has WON with Scissors, against " + oppennetFighter.name);
            return 1;
        }
        else
        {
            System.out.println(LocalTime.now() + ":  " + currentFighter.name + " has LOST to " + oppennetFighter.name);
            return -1;
        }
    }
    public Integer call()
    {
        for(int i = 0; i < opponents.size();i++)
            if(FighterID!=i)
                score += match(this, opponents.get(i));
            finLatch.countDown();
            return score;
    }
}

public class RPSV1 {
    public static void main(String[] args) throws InterruptedException{
        System.out.println("Enter the number of Fighters");
        int numFighters = new Scanner(System.in).nextInt();

        CountDownLatch cLatch = new CountDownLatch(numFighters), fLatch = new CountDownLatch(numFighters);
        List<Fighter> Fighters = new ArrayList<>();
        for(int i = 0; i < numFighters; i++)
            Fighters.add(i, new Fighter(i, cLatch, fLatch, Fighters, "Fighter-" + i));
        ExecutorService pool = Executors.newCachedThreadPool();
            cLatch.await();
        Thread victoryFighter = new Thread(() -> {
            try{
                while(Fighters.size()>1)
                {
                    for (Fighter f : Fighters)
                        f.init();
                    List<Future<Integer>> scores = pool.invokeAll(Fighters);
                    int index = 0;
                    for (int i = 1; i < Fighters.size(); i ++)
                    {
                        if(scores.get(index).get() > scores.get(i).get())
                            index = i;
                    }
                    Fighters.remove(index);
                }
            }
            catch(InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        });
        victoryFighter.start();
        victoryFighter.join();
        System.out.println(LocalTime.now() + ":  " + Fighters.get(0).name + " is the Champion");
        pool.shutdown();
    }
}