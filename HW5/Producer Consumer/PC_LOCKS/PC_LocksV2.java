import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PC_LocksV2
{
    private Queue<Integer> que = new LinkedList();
    private static final int Capacity = 10;
    public boolean print = false;
    private ReentrantLock lock = new ReentrantLock();
    private Condition stackEmptyCondition = lock.newCondition();
    private Condition stackFullCondition = lock.newCondition();

    private void addToQueue() throws InterruptedException
    {
        try
        {
            lock.lock();
            if(que.size() == Capacity)
            {
                stackFullCondition.await();
            }
            int rand = new Random().nextInt(100);
            if(print)
                System.out.println(Thread.currentThread().getName() + " is Producing the value " + rand);
            que.add(rand);
            stackEmptyCondition.signalAll();
        }
        finally
        {
            lock.unlock();
        }
    } 
    private int popFromQueue() throws InterruptedException
    {
        try
        {
            lock.lock();
            if(que.size() == 0)
            {
                stackEmptyCondition.await();
            }
            return que.remove();
        }
        finally
        {
            stackFullCondition.signalAll();
            lock.unlock();
        }
    }
    public static void main(String[] args) {
        System.out.println("Locks");
        System.out.println("5 Producers 2 Consumers");
        boolean print = false;
        PC_LocksV2 object = new PC_LocksV2();
        final ExecutorService poolProducer = Executors.newFixedThreadPool(5);
        final ExecutorService poolConsumer = Executors.newFixedThreadPool(2);
        long StartTime = System.currentTimeMillis();
        poolProducer.execute(() ->{
            for(int i =0; i < 100;i++)
            {
                try
                {
                    object.addToQueue();
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });

        poolConsumer.execute(() ->{
            for(int i =0; i < 100;i++)
            {
                try
                {
                    if(print)
                        System.out.println(Thread.currentThread().getName() + " is Consuming the Value " + object.popFromQueue());
                    else
                        object.popFromQueue();

                    Thread.sleep(1000); // 1 second to consume
                    if(i ==99)
                    {
                        System.out.println("Total Time to complete: " + (System.currentTimeMillis() - StartTime)/1000F + " seconds");
                    }
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });
        poolProducer.shutdown();
        poolConsumer.shutdown();
        
    }
}