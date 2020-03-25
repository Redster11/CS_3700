import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class PC extends Thread
{
    int MaxSize = 10;
    Queue<Integer> que;
    int count = 0;
    Lock lock = new ReentrantLock();
    Condition stackFullCondition = lock.newCondition();
    Condition stackEmptyCondition = lock.newCondition();
    public PC(Queue<Integer> queue, String name, ReentrantLock L1, int count)
    {
        this.setName(name);
        this.que = queue;
        this.lock = L1;
        this.count = count;
        
    }
    public void run()
    {
        while(this.count < 100 || this.que.size() != 0)
        {
            if(this.getName().contains("Producer") && this.count < 100)
            {
                try
                {
                    this.lock.lock();
                    while(this.que.size() >= this.MaxSize)
                    {
                        this.stackFullCondition.await();
                    }
                    int rand = new Random().nextInt(100);
                    this.que.add(rand);
                    this.count++;
                    System.out.println(this.getName() + " has Produced a Value of " + rand);
                    this.stackEmptyCondition.signalAll();
                } 
                catch (InterruptedException e) {

                    e.printStackTrace();
                }
                finally
                {
                    this.lock.unlock();
                }
            }
            else
            {
                try
                {
                    this.lock.lock();
                    while(this.que.size() <=0)
                    {
                        this.stackEmptyCondition.await();
                    }
                    Thread.sleep(1000);
                    System.out.println(this.getName() + " has removed the value of: " + this.que.remove());
                    this.stackFullCondition.signalAll();
                } 
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally
                {
                    this.lock.unlock();
                }
            }
        }
    }
}
public class PC_Locks
{
    public static void main(String[] args) {
        Queue<Integer> CreationArray = new LinkedList();
        ReentrantLock ProducerLock = new ReentrantLock();
        int count = 0;
        // create 5 producer threads
        for(int i = 0; i < 5; i++)
        {
            new PC(CreationArray, "Producer-"+i, ProducerLock, count).start();
        }
        // create 2 Consummer threads
        for(int i =0; i < 2; i++)
        {
            new PC(CreationArray, "Consumer-"+i, ProducerLock, count).start();
        }
        
    }
    
}