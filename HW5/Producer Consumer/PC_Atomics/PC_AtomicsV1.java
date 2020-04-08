import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class PC_AtomicsV1
{
    private AtomicQueue que;
    private int MaxSize, itemsToProduce;
    private AtomicInteger totalConsumed = new AtomicInteger(0);
    private AtomicInteger totalProduced = new AtomicInteger(0);
    private volatile boolean canProduce;
    public PC_AtomicsV1(int numOfPro, int numOfCon, int MaxSize, int itemsProduced)
    {
        this.itemsToProduce = itemsProduced;
        this.MaxSize = MaxSize;
        this.que = new AtomicQueue(MaxSize);
        this.canProduce = true;
        int totalPerPro = (int)Math.floor(itemsProduced/numOfPro);
        int oneExtra = totalPerPro + (itemsProduced%numOfPro);
        Producer[] producers = new Producer[numOfPro];
        Consumer[] consumers = new Consumer[numOfCon];
        producers[0] = new Producer(this, oneExtra, "Producer-0");
        for(int i =1; i < numOfPro; i++)
        {
            producers[i] = new Producer(this, totalPerPro, "Producer-"+i);
        }
        for(int i =0; i < numOfCon; i++)
        {
            consumers[i] = new Consumer(this, "Consumer-"+i);
        }
        for(Producer pro: producers)
        {
            pro.start();
        }
        for(Consumer con: consumers)
        {
            con.start();
        }
        try
        {
            for(Producer pro: producers)
            {
                pro.join();
            }
            canProduce = false;
            for(Consumer con: consumers)
            {
                con.join();
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    public class AtomicQueue
    {
        private final AtomicIntegerArray que;
        private final AtomicInteger positionProducer;
        private final AtomicInteger size;
        private final AtomicInteger positionConsumer;
        int MaxSize;
        public AtomicQueue(int length)
        {
            this.MaxSize = length;
            this.que = new AtomicIntegerArray(length);
            this.positionProducer = new AtomicInteger(0);
            this.positionConsumer = new AtomicInteger(0);
            this.size = new AtomicInteger(0);
        }
        public synchronized boolean Add(int value) throws InterruptedException
        { 
            if(this.positionProducer.get() >= this.MaxSize)
            {
                this.positionProducer.set(0);
            }  
            if(this.size.get() >= this.MaxSize || this.que.get(this.positionProducer.get()) != 0)
            {
                wait();
            }
            if(this.positionProducer.get() >= this.MaxSize)
            {
                this.positionProducer.set(0);
            } 
            if(this.que.compareAndSet(this.positionProducer.intValue(), 0, value))
            {
                this.positionProducer.incrementAndGet();
                totalProduced.incrementAndGet();
                this.size.incrementAndGet();
                notify();
                return true;
            }
            else    
                return false;
            
            
        }
        public synchronized int Remove() throws InterruptedException
        {
            while(totalConsumed.get() <= totalProduced.get() || totalConsumed.get() < 100)
            {
                if(this.positionConsumer.get() >= this.MaxSize)
                {
                    this.positionConsumer.set(0);
                }
                if(this.que.get(this.positionConsumer.get()) == 0 || this.size.get() == 0)
                {
                    if(totalConsumed.get() == 100)
                    {
                        return 0;
                    }
                    wait();
                }
                int item = this.que.get(this.positionConsumer.get());
                this.que.lazySet(this.positionConsumer.get(), 0);
                totalConsumed.incrementAndGet();
                this.positionConsumer.incrementAndGet();
                this.size.decrementAndGet();
                
                notify();
                return item;
            }
            return 0;
        }
    }
    public class Producer extends Thread
    {
        private AtomicInteger total, produced;
        private PC_AtomicsV1 controller;
        Producer(PC_AtomicsV1 controller, int totalToMake, String name)
        {
            this.total = new AtomicInteger(totalToMake);
            this.produced = new AtomicInteger(0);
            this.controller = controller;
            this.setName(name);
        }
        @Override
        public void run()
        {
            try
            {
                while(produced.get() < total.get())
                {
                    int item = new Random().nextInt(100)+1;
                    
                    if(this.controller.que.Add(item))
                    {
                        produced.incrementAndGet();
                        System.out.println(this.getName() + " has Produced the Value " + item);
                    }
                }
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    public class Consumer extends Thread
    {
        private PC_AtomicsV1 controller;
        private boolean running;

        public Consumer(PC_AtomicsV1 controller, String name)
        {
            this.controller = controller;
            this.setName(name);
        }
        @Override
        public void run()
        {
            running = true;
            try
            {
                while(running)
                {
                sleep(1000);//time to consume
                int item = controller.que.Remove();
                if(totalConsumed.get() == itemsToProduce)
                    running = false;
                else
                    System.out.println(this.getName() + " has Consumed the value " + item);
                }
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }

        }
    }
    public static void main(String[] args) 
    {
        long timeS = System.currentTimeMillis();
        PC_AtomicsV1 pc = new PC_AtomicsV1(5, 2, 10, 100);
        long timeE = System.currentTimeMillis();
        System.out.println("Total Time Elapsed " + (timeE - timeS)/1000F + " seconds");  
    }
}