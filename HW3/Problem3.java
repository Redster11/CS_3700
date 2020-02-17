import java.util.Scanner;
import static java.lang.Thread.sleep;
class Official
{
    private String name;
    private int rank;

    public Official(String name, int rank)
    {
        this.name = name;
        this.rank = rank;
    }
    public int getRank()
    {
        return this.rank;
    }
    public String getName()
    {
        return this.name;
    }
    public String getInfo()
    {
        return name + "'s rank is " + rank;
    }
}
class RankThread extends Thread
{
    private volatile Official Leader;
    private int count, maxOfficials;

    public RankThread(int numOfThreads)
    {
        Leader = null;
        maxOfficials = numOfThreads;
    }
    public Official getLeader()
    {
        return Leader;
    }
    public void run()
    {
        synchronized(this)
        {
            try
            {
                while(count < maxOfficials)
                    wait(); // the officials don't need to do anything while after they have found the hiest rank 
            }
            catch(InterruptedException e)
            {
                Leader = null;
                notifyAll(); 
            }
        }
    }
    public void newLeader(Official newHead)
    {
        this.count++;
        // this is to set the new leader of every thread goin through 
        if(Leader == null || Leader.getRank() < newHead.getRank())
        {
            Leader = newHead;
            notifyAll(); // notifies all that the new leader is them
        }
        if(count >= maxOfficials)
            interrupt(); // ends all processes because we cannot mave any more officials
    }
}
class OThread extends Thread
{
    private Official Leader, self;
    private final RankThread rThread;
    private boolean Finished = false;

    public OThread(String name, RankThread rThread)
    {
        setName(name);
        this.rThread = rThread;
        self = new Official(name, (int)((Math.random()-.5)*2*Integer.MAX_VALUE));
        Leader = self;
    }
    public void brodcastLeader()
    {
        Leader = rThread.getLeader();
    }
    public void run()
    {
        System.out.println(getName() + ": Rank: " + self.getRank() + " Guess for Leader: " + Leader.getName());
        synchronized (rThread)
        {
            rThread.newLeader(Leader);
            try
            {
                while(!Finished)
                {
                    rThread.wait();
                    brodcastLeader();
                    if(Leader == null)
                        Finished = true;
                    else
                        System.out.println(self.getName() + " thinks " + Leader.getName() + " is the Leader");
                    
                }
            }
            catch(InterruptedException e)
            {
                System.out.println(getName() + " was interrupted");
            }
        }
    }
}
class MultiThread
{
    public static void main(String[] args) throws InterruptedException
    {
        System.out.println("How many Officials are to be made?");
        Scanner scan = new Scanner(System.in);
        int numOfThreads = scan.nextInt();
        scan.close();
        RankThread rThread = new RankThread(numOfThreads);
        rThread.start();
        sleep((int)(Math.random()*400));
        for(int i = 0; i < numOfThreads; i++)
        {
            new OThread("Leader " + (1+i), rThread).start();
            sleep((int)(Math.random()*400));
        }
        rThread.join();
        System.out.println("Complete Election Selection");
    }
}