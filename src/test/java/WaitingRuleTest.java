import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.math.DoubleMath;

/**
 * A basic example of a rule that waits for some condition to be true before proceeding.
 */
public class WaitingRuleTest {
  
  /**
   * A client that simulates a service that takes some time to
   * boot up.
   */
  public static class Client {
    public Client( long bootTime, TimeUnit unit ) {
      this.unit = unit;
      this.bootTime = bootTime;
    }

    private long startTime = System.currentTimeMillis();
    private TimeUnit unit;
    private long bootTime;
    
    /**
     * Returns true if the service has booted, false otherwise.
     */
    public boolean ready() {
      return startTime + unit.toMillis(bootTime) < System.currentTimeMillis();
    }
  }
  
  /**
   * A rule that creates a client and waits for it to be ready to use.
   */
  public static class ClientRule implements TestRule {
    public ClientRule( long timeout, TimeUnit unit ) {
      this.unit = unit;
      this.timeout = timeout;
    }

    private TimeUnit unit;
    private long timeout;
    Client client;

    @Override
    public Statement apply( Statement base, Description description ) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          ExecutorService executor = Executors.newSingleThreadExecutor();
          try {
            client = executor.submit(() -> {

              Client c = spy(new Client(scale(unit.toMillis(timeout), 0.5d), MILLISECONDS));

              try {
                while( !c.ready() ) {
                  MILLISECONDS.sleep(scale(unit.toMillis(timeout), 0.1d));
                }
              } catch( InterruptedException ie ) {
                Thread.currentThread().interrupt();
                throw new ExecutionException(ie);
              }
              return c;
            }).get(timeout, unit);
          } finally {
            executor.shutdown();
          }

          base.evaluate();
        }
      };
    }
    
    public Client getClient() {
      return client;
    }
    
    private static long scale( long value, double scale ) {
      return DoubleMath.roundToLong(((double)value) * scale, RoundingMode.HALF_UP);
    }
  }
  
  @ClassRule
  public static ClientRule clientRule = new ClientRule(1, SECONDS);

  @Test
  public void clientReady() {
    assertThat(clientRule.getClient().ready(), equalTo(true));
  }

  @Test
  public void readyCalledAboutFiveTimes() {
    verify(clientRule.getClient(), atLeast(4)).ready();
    verify(clientRule.getClient(), atMost(7)).ready();
  }
}
