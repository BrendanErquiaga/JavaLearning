import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * A simple rule example that makes a string available only during a test.
 */
public class SimpleRuleTest {
  public static class SimpleRule implements TestRule {
    String value;
    @Override
    public Statement apply( Statement base, Description description ) {
      return new Statement() {
        @Override public void evaluate() throws Throwable {
          value = "simple";
          try {
            base.evaluate();
          } catch( Throwable t ) {
            value = null;
          }
        }
      };
    }
    
    public String getValue() {
      return value;
    }
  }
  
  @Rule
  public SimpleRule simpleRule = new SimpleRule();
  
  @Test
  public void checkValueDefined() {
    assertThat(simpleRule.getValue(), equalTo("simple"));
  }
}