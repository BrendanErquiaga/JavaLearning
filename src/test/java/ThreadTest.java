import org.junit.Test;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.fge.lambdas.Throwing.consumer;
import static com.github.fge.lambdas.Throwing.runnable;
import static com.github.fge.lambdas.Throwing.intConsumer;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ThreadTest {

    @Test
    public void runsOneThread() throws Exception {
        int loops = 100;
        AtomicInteger count = new AtomicInteger(0);

        Thread t1 = new Thread(()->range(0, loops)
          .forEach(i->count.incrementAndGet()));
 
        t1.start();

        t1.join();

        assertThat(count.get(), equalTo(loops));
    }

    @Test
    public void runsTwoThreads() throws Exception {
        int loops = 100;
        AtomicInteger count = new AtomicInteger(0);

        Thread t1 = new Thread(()->range(0, loops)
          .forEach(i->count.incrementAndGet()));
        t1.start();

        Thread t2 = new Thread(()->range(0, loops)
          .forEach(i->count.incrementAndGet()));
        t2.start();

        t1.join();
        t2.join();

        assertThat(count.get(), equalTo(loops*2));
    }

    public volatile int count = 0;

    @Test
    public void run100ThreadsSynchronized() throws Exception {
        int loops = 10000;
        int threadCount = 100;
        count = 0;
        Runnable r = ()->range(0, loops)
          .forEach(i->{synchronized (this){count++;}});

        List<Thread> threadList = range(0,threadCount)
          .mapToObj(i-> new Thread(r))
          .collect(toList());

        threadList.stream().forEach(Thread::start);
        threadList.stream().forEach(consumer(Thread::join));

        assertThat(count, equalTo(loops*threadCount));
    }

    @Test
    public void run100ThreadsSemaphore() throws Exception {
        int loops = 10000;
        int threadCount = 100;
        State state = new State();
        Semaphore s = new Semaphore(1);
        Runnable r = runnable(()->range(0, loops)
          .forEach(intConsumer(i->{
                s.acquire();
                state.count++;
                s.release();
            })));

        List<Thread> threadList = range(0,threadCount)
          .mapToObj(i-> new Thread(r))
          .collect(toList());

        threadList.stream().forEach(Thread::start);
        threadList.stream().forEach(consumer(Thread::join));

        assertThat(state.count, equalTo(loops*threadCount));
    }

    @Test
    public void run100ThreadsAtomic() throws Exception {
        int loops = 10000;
        int threadCount = 100;
        AtomicReference<Integer> state = new AtomicReference<>(0);
        Runnable r = ()->range(0, loops)
          .forEach(i->state.getAndAccumulate(1,(x,y)->x+y));

        List<Thread> threadList = range(0,threadCount)
          .mapToObj(i-> new Thread(r))
          .collect(toList());

        threadList.stream().forEach(Thread::start);
        threadList.stream().forEach(consumer(Thread::join));

        assertThat(state.get(), equalTo(loops*threadCount));
    }

    @Test
    public void run100ThreadsLocal() throws Exception {
        int loops = 10000;
        int threadCount = 100;
        AtomicReference<Integer> state = new AtomicReference<>(0);
        ThreadLocal<Integer> local = new ThreadLocal<Integer>() {
            public Integer initialValue(){
                return 0;
            }
        };
        Runnable r = ()->{
          range(0, loops)
            .forEach(i->local.set(local.get() + 1));
          state.getAndAccumulate(local.get(),(x,y)->x+y);
        };

        List<Thread> threadList = range(0,threadCount)
          .mapToObj(i-> new Thread(r))
          .collect(toList());

        threadList.stream().forEach(Thread::start);
        threadList.stream().forEach(consumer(Thread::join));

        assertThat(state.get(), equalTo(loops*threadCount));
    }

    public static class State {
        volatile int count = 0;
    }

    @Test
    public void runsNoThread() {
        int loops = 100;
        AtomicInteger count = new AtomicInteger(0);

        range(0, loops)
          .forEach(i->count.incrementAndGet());

        assertThat(count.get(), equalTo(loops));
    }
}
