import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * Shows how to chain rules together and link their results.
 */
public class ChainedRuleTest {
  public static class AppendWordRule implements TestRule {
    private String word;
    private Supplier<String> sentence;
    private String joined;

    public AppendWordRule( String word ) {
      this( null, word );
    }
    public AppendWordRule( Supplier<String> sentence, String word ) {
      this.sentence = sentence;
      this.word = word;
    }

    @Override
    public Statement apply( Statement base, Description description ) {
      return new Statement() {
        @Override public void evaluate() throws Throwable {
          joined = sentence != null ? sentence.get()+" "+word : word;
          try {
            base.evaluate();
          } catch( Throwable t ) {
            joined = null;
          }
        }
      };
    }
    
    public String getJoined() {
      return joined;
    }
  }

  AppendWordRule firstRule = new AppendWordRule("super");
  AppendWordRule secondRule = new AppendWordRule(firstRule::getJoined, "contrived");
  AppendWordRule thirdRule = new AppendWordRule(secondRule::getJoined, "example");
  
  @Rule
  public RuleChain chain = RuleChain
    .outerRule(firstRule)
    .around(secondRule)
    .around(thirdRule);
  
  @Test
  public void testValue() {
    assertThat(thirdRule.getJoined(), equalTo("super contrived example"));
  }
}
