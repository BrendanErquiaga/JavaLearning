import org.junit.Test;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ThreadTest {

    @Test
    public void runsOneThread() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        Thread t1 = new Thread(()-> {
            for(int i = 0; i < 100; i++){
                count.incrementAndGet();
            }
        });
        t1.start();

        t1.join();
        System.out.println(count);
    }

    @Test
    public void runsTwoThreads() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        Thread t1 = new Thread(()-> {
            for(int i = 0; i < 100; i++){
                count.incrementAndGet();
            }
        });
        t1.start();

        Thread t2 = new Thread(()-> {
            for(int i = 0; i < 100; i++){
                count.incrementAndGet();
            }
        });
        t2.start();

        safeRun(t1::join).run();
        safeRun(t2::join).run();
        System.out.println(count);
    }

    public volatile int count = 0;

    @Test
    public void run100ThreadsSynchronized() throws Exception {
        count = 0;
        Runnable r = safeRun(()-> {
            for (int i = 0; i < 10000; i++) {
                synchronized (this){count++;}
            }
        });

        List<Thread> threadList = IntStream.range(0,100).mapToObj(i-> new Thread(r)).collect(Collectors.toList());

        threadList.stream().forEach(t->t.start());
        threadList.stream().forEach(t->{
            try {
                t.join();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(count);
    }

    @Test
    public void run100ThreadsSemaphore() throws Exception {
        State state = new State();
        Semaphore s = new Semaphore(1);
        Runnable r = safeRun(()-> {
            for (int i = 0; i < 10000; i++) {
                s.acquire();
                state.count++;
                s.release();
            }
        });

        List<Thread> threadList = IntStream.range(0,100).mapToObj(i-> new Thread(r)).collect(Collectors.toList());

        threadList.stream().forEach(t->t.start());
        threadList.stream().forEach(t->{
            try {
                t.join();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("MY COUNT: " + state.count);
    }

    @Test
    public void run100ThreadsAtomic() throws Exception {
        AtomicReference<Integer> state = new AtomicReference<>(0);
        Runnable r = safeRun(()-> {
            for (int i = 0; i < 10000; i++) {
                state.getAndAccumulate(1,(x,y)->x+y);
            }
        });

        List<Thread> threadList = IntStream.range(0,100).mapToObj(i-> new Thread(r)).collect(Collectors.toList());

        threadList.stream().forEach(t->t.start());
        threadList.stream().forEach(t->{
            try {
                t.join();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("MY COUNT: " + state.get());
    }

    @Test
    public void run100ThreadsLocal() throws Exception {
        AtomicReference<Integer> state = new AtomicReference<>(0);
        ThreadLocal<Integer> local = new ThreadLocal() {
            public Integer initialValue(){
                return 0;
            }
        };
        Runnable r = safeRun(()-> {
            for (int i = 0; i < 10000; i++) {
                local.set(local.get() + 1);
            }
            state.getAndAccumulate(local.get(),(x,y)->x+y);
        });

        List<Thread> threadList = IntStream.range(0,100).mapToObj(i-> new Thread(r)).collect(Collectors.toList());

        threadList.stream().forEach(t->t.start());
        threadList.stream().forEach(t->{
            try {
                t.join();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("MY COUNT: " + state.get());
    }

    public static class State {
        volatile int count = 0;
    }

    public static interface ThrowingRunnable {
        public void run() throws Exception;
    }

    public static Runnable safeRun( ThrowingRunnable c ) {
        return () ->{ try {
            c.run();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }   };
    }

    @Test
    public void runsNoThread() {
        AtomicInteger count = new AtomicInteger(0);


        for(int i = 0; i < 100; i++){
            count.incrementAndGet();
        }


        System.out.println(count);
    }
}
