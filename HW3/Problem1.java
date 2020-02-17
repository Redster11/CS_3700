import java.lang.Math;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
class Sock 
{
     private String color;
     public Sock(String color)
     {
          this.color = color;
     }
     public String getName()
     {
          return this.color;
     }
}
class SockMaker extends Thread 
{
     private String color;
     private String name;
     private int maxSocks, socksMade;
     private CountDownLatch numOfThreads;
     private BlockingQueue<Sock> matcherQue;
     SockMaker(String thread, BlockingQueue<Sock> sockMatchingQue, CountDownLatch latch) {
          this.color = thread;
          this.name = thread + " Sock";
          this.setName(name);
          this.maxSocks = (int) (Math.random() * (100 + 1));
          this.socksMade = 0;
          matcherQue = sockMatchingQue;
          this.numOfThreads = latch;
     }
     public void run() {
          try {
               while (socksMade < maxSocks) {
                    socksMade = socksMade + 1;
                    System.out.println(this.name + ": Produced " + this.socksMade + " of " + this.maxSocks + " "
                              + this.name + "s");
                    matcherQue.put(new Sock(this.color));
                    sleep(100);
               }
          } catch (final InterruptedException e) {
               System.out.println(this.name + "Interrupted");
          }
          numOfThreads.countDown();
     }
}

class Matcher extends Thread {
     private final BlockingQueue<Sock> matchQue;
     private final BlockingQueue<Sock[]> washerQue;
     private Sock red, blue, green, orange;
     private int total;
     private boolean running = true;
     public Matcher(BlockingQueue<Sock> mQue, BlockingQueue<Sock[]> wQue) {
          this.matchQue = mQue;
          this.washerQue = wQue;
          this.setName("Matching Thread");
     }
     public void finish()
     {
          running = false;
     }
     public void run()
     {
          try
          {    
               while(running || !matchQue.isEmpty())
               {
                    Sock temp = matchQue.poll(100, TimeUnit.MILLISECONDS);
                    if(temp != null)
                    {
                         if(temp.getName() == "Red")
                         {
                              if(red == null)
                                   red = temp;
                              else
                              {
                                   washerQue.put(new Sock[]{red,temp});
                                   red = null;
                                   total = total +2;
                                   System.out.println(this.getName()+" : Send Red Socks to Washer. Total socks " + total + ". Total inside queue "+washerQue.size());
                              }
                         }
                         else if(temp.getName() == "Blue")
                         {
                              if(blue == null)
                                   blue = temp;
                              else
                              {
                                   washerQue.put(new Sock[]{blue,temp});
                                   blue = null;
                                   total = total +2;
                                   System.out.println(this.getName()+" : Send Blue Socks to Washer. Total socks " + total + ". Total inside queue "+washerQue.size());
                              }
                         }
                         else if(temp.getName() == "Green")
                         {
                              if(green == null)
                                   green = temp;
                              else
                              {
                                   washerQue.put(new Sock[]{green,temp});
                                   green = null;
                                   total = total +2;
                                   System.out.println(this.getName()+" : Send Green Socks to Washer. Total socks " + total + ". Total inside queue "+washerQue.size());
                              }
                         }
                         else
                         {
                              if(orange == null)
                                   orange = temp;
                              else
                              {
                                   washerQue.put(new Sock[]{orange,temp});
                                   orange = null;
                                   total = total +2;
                                   System.out.println(this.getName()+" : Send Orange Socks to Washer. Total socks " + total + ". Total inside queue "+washerQue.size());
                              }
                         }

                    }
               }
          }
          catch(final InterruptedException e) {
               e.printStackTrace();
          }
     }
}

class Washer extends Thread {
     private final BlockingQueue<Sock[]> washerQue;
     private boolean running = true;
     public Washer(final BlockingQueue<Sock[]> wQue) {
          this.washerQue = wQue;
          this.setName("Washer Thread");
     }
     public void finish()
     {
          this.running = false;
     }
     public void run() {
          try {
               while (running || !washerQue.isEmpty()) {
                    final Sock[] temp = washerQue.poll(100, TimeUnit.MILLISECONDS);
                    if (temp != null)
                         System.out.println(this.getName() + ": Destroyed " + temp[0].getName() + "socks");
               }
          } catch (final InterruptedException e) {
               e.printStackTrace();
          }
     }
}

class MultiThread {
     public static void main(final String args[]) 
     {
          System.out.println("Strating Threads");
          BlockingQueue<Sock> matchingQue = new ArrayBlockingQueue<Sock>(400);
          BlockingQueue<Sock[]> washerQue = new ArrayBlockingQueue<Sock[]>(200);

          CountDownLatch latch = new CountDownLatch(4);

          SockMaker red = new SockMaker("Red", matchingQue, latch);
          SockMaker blue = new SockMaker("Blue", matchingQue, latch);
          SockMaker green = new SockMaker("Green", matchingQue, latch);
          SockMaker orange = new SockMaker("Orange", matchingQue, latch);

          Matcher match = new Matcher(matchingQue, washerQue);
          Washer wash = new Washer(washerQue);

          match.start();
          wash.start();
          red.start();
          blue.start();
          green.start();
          orange.start();
          try
          {
               latch.await();
               match.finish();
               match.join();
               wash.finish();
          }
          catch(InterruptedException e)
          {
               e.printStackTrace();
          }
     }
}