import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class PC_IsolationV1{
    private Queue<Integer> que;
    private int MaxSize = 10;
    private volatile boolean canProduce;

    public PC_IsolationV1(int numOfPro, int numOfCon, int MaxSize, int itemsProduced, boolean print)
    {
        this.que = new LinkedList<>();
        this.MaxSize = MaxSize;
        this.canProduce = true;
        int totalPerPro = (int)Math.floor(itemsProduced/numOfPro);
        int oneExtra = totalPerPro + (itemsProduced%numOfPro);
        Producer[] producers = new Producer[numOfPro];
        Consumer[] consumers = new Consumer[numOfCon];
        producers[0] = new Producer(this, oneExtra, "Producer-0", print);
        for(int i =1; i < numOfPro; i++)
        {
            producers[i] = new Producer(this, totalPerPro, "Producer-"+i, print);
        }
        for(int i =0; i < numOfCon; i++)
        {
            consumers[i] = new Consumer(this, "Consumer-"+i, print);
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
        private boolean print;

        public Consumer(PC_IsolationV1 controller, String name, boolean print)
        {
            this.controller = controller;
            this.setName(name);
            this.print = print;
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
                        if(print)
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
        private boolean print;

        Producer(PC_IsolationV1 controller, int finalTotal, String ID, boolean print)
        {

            total = finalTotal;
            this.controller = controller;
            this.setName(ID);
            this.print = print;
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
                    if(print)
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
        System.out.println("Isolation");
        System.out.println("5 Producers 2 Consumers");
        long timeS = System.currentTimeMillis();
        PC_IsolationV1 pc1 = new PC_IsolationV1(5, 2, 10, 100, false);
        long timeE = System.currentTimeMillis();
        System.out.println("Total Time Elapsed " + (timeE - timeS)/1000F + " seconds");

        System.out.println("\n2 Producers 5 Consumers");
        timeS = System.currentTimeMillis();
        PC_IsolationV1 pc2 = new PC_IsolationV1(2, 5, 10, 100, false);
        timeE = System.currentTimeMillis();
        System.out.println("Total Time Elapsed " + (timeE - timeS)/1000F + " seconds");
    }
}