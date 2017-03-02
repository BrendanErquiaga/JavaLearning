import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.fge.lambdas.Throwing.consumer;
import static com.github.fge.lambdas.Throwing.runnable;
import static com.github.fge.lambdas.Throwing.intConsumer;
import static com.github.fge.lambdas.Throwing.function;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ExcecutorTest {

    @Test
    public void simpleExcecutor() throws Exception {
        ExecutorService e = Executors.newCachedThreadPool();

        Future<String> f = e.submit(()->"done");

        assertThat(f.get(), equalTo("done"));

        e.shutdown();
        e.awaitTermination(10, TimeUnit.MILLISECONDS);
    }

    @Test
    public void executorOverload() throws Exception {
        ExecutorService e = Executors.newFixedThreadPool(10);

        List<Integer> list = IntStream.range(0,100).mapToObj(i-> (Callable<Integer>)()-> i + 1).map(e::submit).map(function(Future::get)).collect(Collectors.toList());

        assertThat(list, equalTo(IntStream.range(1,101).mapToObj(Integer::valueOf).collect(Collectors.toList())));

        e.shutdown();
        e.awaitTermination(10, TimeUnit.MILLISECONDS);
    }

    @Test
    public void executorScheduled() throws Exception {
        ScheduledExecutorService e = Executors.newScheduledThreadPool(10);

        AtomicInteger runCount = new AtomicInteger(10);
        ConcurrentLinkedQueue<Long> timeList = new ConcurrentLinkedQueue<>(new ArrayList<>());
        ScheduledFuture<?> f = e.scheduleAtFixedRate(runnable(()-> {
            timeList.add(System.currentTimeMillis());
            if(runCount.decrementAndGet() <= 0){
                throw new RuntimeException("Termination");
            }
            TimeUnit.MILLISECONDS.sleep(10);
        }), 0, 1, TimeUnit.MILLISECONDS);

        try {
            f.get();
        } catch (Exception exception) {}

        Long[] t = timeList.toArray(new Long[0]);

        assertThat(t.length, equalTo(10));

        for(int i = 1; i < t.length; i++){
            assertThat(t[i], greaterThan(t[i-1]));
        }

        e.shutdown();
        e.awaitTermination(10, TimeUnit.MILLISECONDS);
    }

}
