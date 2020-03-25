import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class PC_IsolationV1{
    private Queue<Integer> que;
    private int MaxSize = 10;
    private volatile boolean canProduce;

    public PC_IsolationV1(int numOfPro, int numOfCon, int MaxSize, int itemsProduced)
    {
        this.que = new LinkedList<>();
        this.MaxSize = MaxSize;
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

    public synchronized Integer remove() throws InterruptedException
    {
        Integer item = null;
        while(que.isEmpty())
        {
            if(!canProduce)
            {
                notifyAll();
                return null;
            }
            wait();
        }
        item = que.remove();
        notifyAll();
        return item;
    }
    public synchronized void add(Integer item) throws InterruptedException
    {
        while(MaxSize <= que.size())
        {
            wait();
        }
        que.add(item);
        notifyAll();
    }
    private class Consumer extends Thread
    {
        private PC_IsolationV1 controller;
        private boolean running;

        public Consumer(PC_IsolationV1 controller, String name)
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
                    Integer item = controller.remove();
                    if(item == null)
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
    public class Producer extends Thread
    {
        private int total, produced;
        private PC_IsolationV1 controller;

        Producer(PC_IsolationV1 controller, int finalTotal, String ID)
        {
            total = finalTotal;
            this.controller = controller;
            this.setName(ID);
        }

        @Override
        public void run()
        {
            try
            {
                while(produced < total)
                {
                    int item = new Random().nextInt(100);
                    ++produced;
                    controller.add(item);
                    System.out.println(this.getName() + " has Produced the Value " + item);
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
        PC_IsolationV1 pc = new PC_IsolationV1(5, 2, 10, 100);
        long timeE = System.currentTimeMillis();
        System.out.println("Total Time Elapsed " + (timeE - timeS)/1000F + " seconds");
    }
}