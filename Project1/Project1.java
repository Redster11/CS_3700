import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

class LetterNode implements Comparable<LetterNode> {
    int frequency;
    char Leter;
    LetterNode left, right;

    public int compareTo(LetterNode node) {
        return frequency - node.frequency;
    }
}
class EncoderThread implements Runnable
{
    protected String File;
    protected String Encoded;
    BlockingQueue<String> bQueue;
    public EncoderThread(String text)
    {
        this.File = text;
    }
    public void run()
    {
        System.out.println("Start Encoding..." + LocalTime.now());
        long startTime = System.currentTimeMillis();
        this.Encoded = Project1.encode(this.File);
        System.out.println("End Encoding..." + LocalTime.now());
        System.out.println("Total Time taken: " + (System.currentTimeMillis() - startTime) + " ms");
    }
}
class ReaderThread implements Runnable
{
    String FileName = "D:\\GitHub\\PracticeCode\\const.txt";
    protected BlockingQueue<String> bQueue = null;
    public ReaderThread(BlockingQueue<String> bQueue, String File)
    {
        this.bQueue = bQueue;
    }
    public void run()
    {
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(new File(FileName)));
            String buffer = null;
            while((buffer=br.readLine())!=null)
            {
                bQueue.put(buffer);
            }
            bQueue.put("EOF"); // end of file
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {

        }
        finally
        {
            try
            {
                br.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class WriteStringThread implements Runnable
{
    protected File file = new File("ConstEncoded.txt");
    FileWriter fr = null;
    BufferedWriter br = null;
    int numLine;
    String data = null;
    String datawithNL = null;
    WriteStringThread(String data, int numLine)
    {
        this.numLine = numLine;
        this.data = data;
        this.datawithNL = this.data+System.getProperty("line.separator");
    }
    public void run()
    {
        try
        {
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);
            for (int i = numLine; i > 0; i--)
            {
                br.write(datawithNL);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                br.close();
                fr.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();    
            }
        }
    }
}
class WriterThread implements Runnable
{
    protected BlockingQueue<String> bQueue = null;
    String FileToWrite = null;
    public WriterThread(String File)
    {
        this.FileToWrite = File;

    }
    public WriterThread(BlockingQueue<String> bQueue)
    {
        this.bQueue = bQueue;
    }
    public void run()
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(new File("ConstEncoded.txt"));
            while(true)
            {
                String buffer = bQueue.take();
                if(buffer.equals("EOF")) // we have reached the end of the file
                {
                    break;
                }
                writer.println(buffer);
            }
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
public class Project1 extends Thread{
    private static Map<Character, String> letterPrefixMap = new HashMap<>();
    static LetterNode root;
    static int numLines;

    private static LetterNode buildTree(Map<Character, Integer> freq) {
        PriorityQueue<LetterNode> pQueue = new PriorityQueue<>();
        Set<Character> keySet = freq.keySet();
        for (Character c : keySet) {
            LetterNode huffman = new LetterNode();
            huffman.Leter = c;
            huffman.frequency = freq.get(c);
            huffman.left = null;
            huffman.right = null;
            pQueue.offer(huffman);
        }
        assert pQueue.size() > 0;
        while (pQueue.size() > 1) {
            LetterNode x = pQueue.peek();
            pQueue.poll(); // grabbing the lowest element

            LetterNode y = pQueue.peek();
            pQueue.poll(); // grabbing second lowest element

            LetterNode SUM = new LetterNode();

            SUM.frequency = x.frequency + y.frequency;
            SUM.Leter = '-';
            SUM.left = x;
            SUM.right = y;
            root = SUM;
            pQueue.offer(SUM);
        }
        return pQueue.poll();
    }

    private static void setPrefixCode(LetterNode node, StringBuilder let) {
        if (node != null) {
            if (node.left == null && node.right == null) {
                letterPrefixMap.put(node.Leter, let.toString());
            } else {
                let.append('0');
                setPrefixCode(node.left, let);
                let.deleteCharAt(let.length() - 1);

                let.append('1');
                setPrefixCode(node.right, let);
                let.deleteCharAt(let.length() - 1);
            }
        }
    }

    public static String encode(String input) {
        Map<Character, Integer> freq = new HashMap<>();
        for (int i = 0; i < input.length(); i++) {
            if (!freq.containsKey(input.charAt(i))) {
                freq.put(input.charAt(i), 0);
            }
            freq.put(input.charAt(i), freq.get(input.charAt(i)) + 1);
        }
        System.out.println("Char Frequency Map = " + freq);
        root = buildTree(freq);

        setPrefixCode(root, new StringBuilder());
        System.out.println("Char Prefix Map = " + letterPrefixMap);
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            s.append(letterPrefixMap.get(c));
        }
        return s.toString();
    }

    private static String readFile(String filePath) 
    {
        StringBuilder contentBuilder = new StringBuilder();
        int length = 0;
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
            length++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Project1.numLines = length;
        return contentBuilder.toString();
    }
    public static void main(String[] args) throws FileNotFoundException
    {
        String test = "ABCD12%%2345";
        System.out.println("Original Text = " + test);
        String s = encode(test);
        System.out.println(s);
        System.out.println("The size of the original file is: 44.0 KB (45,056 bytes)");
        String File = null;
        String filePath = "D:\\GitHub\\PracticeCode\\const.txt";
        File = Project1.readFile(filePath);
        EncoderThread encodeThread = new EncoderThread(File);
        encodeThread.run();
        WriteStringThread writer = new WriteStringThread(encodeThread.Encoded, Project1.numLines);
        writer.run();
        //BlockingQueue<String> queue = new ArrayBlockingQueue<>(1024);
        //ReaderThread reader = new ReaderThread(queue, encodeThread.Encoded);
        //WriterThread writer = new WriterThread(queue);
       // new Thread(reader).start();
        //new Thread(writer).start();
    }
}