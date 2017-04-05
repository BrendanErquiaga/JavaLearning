import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * A set of tests where the logic binds JSON from some source and then handles that binding,
 * with some light error handling.  There is variance both in the sourcing of data and in
 * the type it is bound to.  We can apply some different patterns to this and see if
 * we can make the logic more clear.
 * 
 * @author Christian Trimble
 *
 */
public class StrategyPatternTest {

  public static interface IOSupplier{
    public Reader get() throws IOException;
  }

  public static class Context <T>{

    ObjectMapper mapper = new ObjectMapper();

    Consumer<T> handler;

    IOSupplier supplier;

    TypeReference<T> typeReference;

    public Context(TypeReference<T> typeReference, IOSupplier supplier, Consumer<T> handler) {
      this.handler = handler;
      this.supplier = supplier;
      this.typeReference = typeReference;
    }

    public void execute(){
      try( Reader in = supplier.get()) {
        T values = mapper.readValue(in, typeReference);

        handler.accept(values);
      } catch( IOException ioe ) {
        throw new UncheckedIOException("could not handle resource.", ioe);
      }
    }
  }
  
  ObjectMapper mapper = new ObjectMapper();

  IOSupplier fromClassLoader = ()-> new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("string-array.json"));
  IOSupplier fromClass = ()-> new InputStreamReader(this.getClass().getResourceAsStream("string-array.json"));
  IOSupplier fromString = ()-> new StringReader("[\"one\", \"two\", \"three\"]");
  TypeReference<List<String>> listStringRef = new TypeReference<List<String>>(){};
  TypeReference<ArrayNode> arrayNodeRef = new TypeReference<ArrayNode>(){};
  Consumer<List<String>> arrayListConsumer = (values)->assertThat(values, equalTo(newArrayList("one", "two", "three")));
  Consumer<ArrayNode> arrayNodeConsumer = (values)->assertThat(values, equalTo(arrayNode("one", "two", "three")));

  @Test
  public void handleResourceFromClasspathAsList() {
    new Context<List<String>>(listStringRef, fromClassLoader, arrayListConsumer).execute();
  }
  
  @Test
  public void handleResourceFromClasspathAsNode() {
    new Context<ArrayNode>(arrayNodeRef, fromClassLoader, arrayNodeConsumer).execute();
  }
  
  @Test
  public void handleResourceFromClasspathRelativeToClassAsList() {
    new Context<List<String>>(listStringRef, fromClass, arrayListConsumer).execute();
  }
  @Test
  public void handleResourceFromClasspathRelativeToClassAsNode() {
    new Context<ArrayNode>(arrayNodeRef, fromClass, arrayNodeConsumer).execute();
  }

  @Test
  public void handleResourceFromStringAsList() {
    new Context<List<String>>(listStringRef, fromString, arrayListConsumer).execute();
  }

  @Test
  public void handleResourceFromStringAsNode() {
    new Context<ArrayNode>(arrayNodeRef, fromString, arrayNodeConsumer).execute();
  }
  
  private static ArrayNode arrayNode( String... values) {
    ArrayNode result = JsonNodeFactory.instance.arrayNode();
    Arrays.asList(values).stream().forEach(result::add);
    return result;
  }
}
