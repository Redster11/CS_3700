import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
class Producer extends Thread {

    private BlockingQueue servingLine;
    private int startingPoint;
    public Producer(BlockingQueue servingLine, int start) {
        this.servingLine = servingLine;
        this.startingPoint = start;
    }

    public void run() {
        for (int i=this.startingPoint; i<this.startingPoint+50; i++) 
        { // serve 100 bowls of soup
            try {
                if(servingLine.remainingCapacity() > 2)
                {
                servingLine.add("Bowl #" + i);
                System.out.format("Served Bowl #%d - remaining capacity: %d\n", i, servingLine.remainingCapacity()-2);
                }
                else
                    i--;
                    Thread.sleep(100);
            } 
            catch (Exception e) { e.printStackTrace(); }
        }
        servingLine.add("no More");
        servingLine.add("no More");
        servingLine.add("no More");
        //System.out.println("Finished");
    }
}
class Consumer extends Thread {
    private BlockingQueue servingLine;

    public Consumer(BlockingQueue servingLine) {
        this.servingLine = servingLine;
        
    }

    public void run() {
        while (true) {
            try {
                String bowl = null;
                if(servingLine.size() >0)
                {
                    bowl = (String)servingLine.take();
                    if (bowl == "no More")
                        break;
                    
                    else
                    {
                        System.out.format("Ate %s - Remaining Capacity: %d\n", bowl,servingLine.remainingCapacity()-2);
                        Thread.sleep(1000); // time to consume item
                    }
                }
                else
                    Thread.sleep(10);
             } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
public class PC_BlockingQue {
    public static void main(String args[]) {
        BlockingQueue servingLine = new ArrayBlockingQueue<String>(10);
        new Consumer(servingLine).start();
        new Consumer(servingLine).start();
        new Consumer(servingLine).start();
        new Consumer(servingLine).start();
        new Consumer(servingLine).start();

        new Producer(servingLine,0).start();
        new Producer(servingLine,50).start();
    }
}