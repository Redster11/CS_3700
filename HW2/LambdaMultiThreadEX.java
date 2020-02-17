import java.util.Arrays;
import java.util.function.Consumer;

public class LambdaMultiThreadEX {
    public static void main(String[] args) {
        Runnable r1 = new Runnable(){
        
            @Override
            public void run() {
                System.out.println("run 1");
                
            }
        };
        Runnable r2 = () -> System.out.println("run 2");

        r1.run();
        r2.run();

        Consumer<String> hello = name -> System.out.println("Hello, " + name);
        for (String name : Arrays.asList("Duke", "Mickey", "Minnie")) {
                hello.accept(name);
        }
    }
} 