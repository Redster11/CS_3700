import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class ReaderThreadReverse implements Runnable {
    protected BlockingQueue<String> bQueue = null;
    protected String Dec = null;
    protected String[] Sentences = null;

    public ReaderThreadReverse(BlockingQueue<String> bQueue) {
        this.bQueue = bQueue;
    }

    private static String readAll(String filePath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    String FileName = "D:\\GitHub\\CS_3700\\HW4\\Declaration.txt";

    public void run() {
        System.out.println("Reading file...");
        Dec = readAll(FileName);
        Sentences = Dec.split(".  ");

        for (int i = 0; i < Sentences.length-1; i++) {
            String[] temp = Sentences[i].split(" ");
            String concat = null;
            for (int j = temp.length-1; j > -1; j--) {
                if(j == temp.length-1)
                    concat = temp[j];
                else
                    concat += temp[j];
                if(j != 0)
                    concat += " ";
            }
            try {
                concat += ".  ";
                bQueue.put(concat);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            bQueue.put("EOF");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
class WriterThreadReverse implements Runnable
{
    protected BlockingQueue<String> bQueue = null;
    String FileToWrite = null;
    public WriterThreadReverse(BlockingQueue<String> bQueue)
    {
        this.bQueue = bQueue;
    }
    public void run()
    {
        System.out.println("Writing File...");
        PrintWriter writer = null;
        try
        {
            long startTime = System.currentTimeMillis();
            writer = new PrintWriter(new File("D:\\GitHub\\CS_3700\\HW4\\backwards.txt"));
            while(true)
            {
                String buffer = bQueue.take();
                if(buffer.equals("EOF")) // we have reached the end of the file
                {
                    break;
                }
                writer.println(buffer);
            }
            System.out.println("Time taken to write file was: " + (System.currentTimeMillis() - startTime)+"ms");
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {

        }
        finally
        {
            writer.close();
        }
    }
}
public class Reverse extends Thread
{
    public static void main(String[] args) {
        BlockingQueue<String> bQueue = new ArrayBlockingQueue<>(4096);
        ReaderThreadReverse reader = new ReaderThreadReverse(bQueue);
        WriterThreadReverse writer = new WriterThreadReverse(bQueue);
        new Thread(reader).start();
        new Thread(writer).start();
    }
}