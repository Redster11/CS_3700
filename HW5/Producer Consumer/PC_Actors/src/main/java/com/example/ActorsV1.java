package com.example;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ActorSystem;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import javax.sound.midi.Receiver;
public class ActorsV1 
{
    public enum Commands 
    {
        REQUEST_OBJECT, READY, CONSUME
    }
    
    static class Buffer extends AbstractActor {
        private final int size, producedAmount;
        private Queue<Integer> que;
        private int consumedAmount;
        private boolean isCreating = true;

        public static Props props(int bufferSize, int producedAmount) {
            return Props.create(Buffer.class, () -> new Buffer(bufferSize, producedAmount));
        }

        private Buffer(int bufferSize, int producedAmount) {
            this.size = bufferSize;
            this.producedAmount = producedAmount;
            this.que = new LinkedList<>();
        }

        public AbstractActor.Receive createReceive() {
            return receiveBuilder().match(Integer.class, this::addItem).matchEquals(Commands.READY, this::consume)
                    .matchEquals(Commands.CONSUME, (c) -> EndProduction()).build();
        }

        private void EndProduction() {
            consumedAmount++;
            if (consumedAmount >= producedAmount)
                isCreating = false;
        }

        private void consume(Commands c) {
            if (que.isEmpty()) {
                if (isCreating) {
                    getSelf().forward(c, getContext());
                } else {
                    getContext().getSystem().terminate();
                }
            } else {
                getSender().tell(que.remove(), getSelf());
            }
        }

        private void addItem(Integer item) {
            if (que.size() >= size) {
                getSelf().forward(item, getContext());
            } else {
                que.add(item);
                getSender().tell(Commands.REQUEST_OBJECT, getSelf());
            }
        }
    }
    
    static class Consumer_Actor extends AbstractActor {
        private final ActorRef buffer;
        private final boolean print;
        private final int delay; // time to consume

        public static Props props(ActorRef Buffer, int delay, boolean print) {
            return Props.create(Consumer_Actor.class, () -> new Consumer_Actor(Buffer, delay, print));
        }

        private Consumer_Actor(ActorRef Buffer, int delay, boolean print) {
            this.print = print;
            this.delay = delay;
            buffer = Buffer;
            buffer.tell(Commands.READY, getSelf());
            getContext().watch(buffer);
        }

        public AbstractActor.Receive createReceive() {
            return receiveBuilder().match(Integer.class, this::consume).build();
        }

        private void consume(Integer item) {
            if (print) {
                System.out.println("Consumed " + item);
            }
            try {
                Thread.sleep(delay);
                buffer.tell(Commands.READY, getSelf());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    static class Producer_Actor extends AbstractActor {
        private final String name;
        private int producedAmount, maxItemCount;
        private final boolean print;
        private boolean isProducing = true;

        public static Props props(String name, int maxItemCount, boolean print) {
            return Props.create(Producer_Actor.class, () -> new Producer_Actor(name, maxItemCount, print));
        }

        private Producer_Actor(String name, int maxItemCount, boolean print) {
            this.name = name;
            this.maxItemCount = maxItemCount;
            this.print = print;
            producedAmount = 0;
        }

        @Override
        public AbstractActor.Receive createReceive() {
            return receiveBuilder().matchEquals(Commands.REQUEST_OBJECT, (c) -> {
                if (isProducing)
                    produce();
            }).build();
        }

        private void produce() {
            producedAmount++;
            Integer item = new Random().nextInt(100);
            getSender().tell(item, getSelf());
            if (print) {
                System.out.println(name + ": Made " + item);
            }
            if (producedAmount >= maxItemCount) {
                getSender().tell(Commands.CONSUME, getSelf());
                isProducing = false;
            }
        }
    }
    private volatile boolean running = true;
    public ActorsV1(int producerCount, int consumerCount, int bufferSize, int itemCount, int sleepDelay, boolean print)
    {
        ActorSystem sys = ActorSystem.create("Producer_Consumer");

        ActorRef buffer = sys.actorOf(Buffer.props(bufferSize, producerCount), "buffer");
        int itemCountFix = (int) Math.floor(itemCount/producerCount);
        sys.actorOf(Producer_Actor.props(("Producer" + 0), itemCountFix + itemCount % producerCount , print), "Producer-"+ 0).tell(Commands.REQUEST_OBJECT, buffer);
        for (int i = 1; i < producerCount; i++)
        {
            sys.actorOf(Producer_Actor.props(("Producer" + i), itemCountFix, print), "Producer-"+ i).tell(Commands.REQUEST_OBJECT, buffer);
        }

        for (int i = 0; i < consumerCount; i++)
        {
            sys.actorOf(Consumer_Actor.props(buffer, sleepDelay, print), "Consumer-"+i);
        }

        sys.registerOnTermination(() -> {
            running = false;
        });

        while(running) ;
        if(print)
            System.out.println("Finished");
    }
    public static void main(String[] args) 
    {
        System.out.println("Actors");
        System.out.println("5 Producers, 2 Consumers");
        long Start_time = System.currentTimeMillis();
        new ActorsV1(5, 2, 10, 100, 1000, false);
        long end_time = System.currentTimeMillis() - Start_time;
        System.out.println("Total Time Elapsed: " + end_time/1000F + " seconds"); 

        System.out.println("2 Producers, 5 Consumers");
        Start_time = System.currentTimeMillis();
        new ActorsV1(2, 5, 10, 100, 1000, false);
        end_time = System.currentTimeMillis() - Start_time;
        System.out.println("Total Time Elapsed: " + end_time/1000F + " seconds");
    }
}