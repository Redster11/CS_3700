package com.example;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ActorSystem;

public class Actors extends AbstractActor
{
    public enum Commands 
    {
        KILL,READY, REQUEST_OBJECT
    }
    private final int primeNumber;   // this is going to be the first number that has not been canceled out
    private ActorRef nextPrimeNumber;    // this is going to be the second open number (needs to have already been passed by first)
    public static Props props(int primeNumber)
    {
        return Props.create(Actors.class, () -> new Actors(primeNumber));
    }
    private Actors(int primeNumber)
    {
        this.primeNumber = primeNumber;
        System.out.print(primeNumber + "\t");
    }
    public Receive createReceive() 
    {
        return receiveBuilder()
            .match(Integer.class, this::CurrentNumber)
            .matchEquals(Commands.KILL, this::kill).build();
    }
    private void kill(Commands c)
    {
        if(nextPrimeNumber == null)
            getContext().getSystem().terminate(); // if the current value is non prime end
        else   
            nextPrimeNumber.forward(c, getContext()); // if the current number is a prime multiply it to get all of the values that match the number.
    }
    private void CurrentNumber(int number)
    {
        if(number % primeNumber != 0)
        {
            nextPrimeNumber = getContext().actorOf(Actors.props(number), "Prime_Actor-" + number); //since the actors are going to be working on many different numbers I am naming the actors as such
        }
        else
        {
            nextPrimeNumber.tell(number, ActorRef.noSender()); // this means that the value is a prime
        }
    }
    private static volatile boolean isRunning = true;
    public static void run(int maxValue)
    {
        ActorSystem sys = ActorSystem.create("Primes");
        ActorRef root = sys.actorOf(Actors.props(2), "Prime_Actor-2");
        for(int i = 3; i < maxValue; i++)
        {
            root.tell(i, ActorRef.noSender());
        }
        root.tell(Commands.KILL, ActorRef.noSender());

        sys.registerOnTermination(() -> {
            isRunning = false;
        });

        while(isRunning) ; // waits for all actors to terminate 
        System.out.println("\nFinished");
    }
    public static void main(String[] args) 
    {
           System.out.println("Prime Actors");
           long Start_time = System.currentTimeMillis();
           Actors.run(1_000_000);
           long end_time = System.currentTimeMillis() - Start_time;
           System.out.println("time elapsed: " + end_time/1000F + " seconds"); 
    }
}