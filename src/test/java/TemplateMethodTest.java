import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

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
public class TemplateMethodTest {

  public static abstract class HandlerTemplate <T> {

    private TypeReference<T> typeReference;
    ObjectMapper mapper = new ObjectMapper();

    public HandlerTemplate(TypeReference<T> typeReference){
      this.typeReference = typeReference;
    }

    public final void execute() {
      try( Reader in = getReader()) {
        T values = mapper.readValue(in, typeReference);
        handle(values);
      } catch( IOException ioe ) {
        throw new UncheckedIOException("could not handle resource.", ioe);
      }
    }

    public abstract Reader getReader() throws IOException;

    public abstract void handle(T values);
  }
  
  ObjectMapper mapper = new ObjectMapper();
  
  @Test
  public void handleResourceFromClasspathAsList() {
    new HandlerTemplate<List<String>>(new TypeReference<List<String>>(){}){
      @Override
      public Reader getReader() throws IOException {
        return new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("string-array.json"));
      }

      @Override
      public void handle(List<String> values) {
        assertThat(values, equalTo(newArrayList("one", "two", "three")));
      }
    };
  }
  
  @Test
  public void handleResourceFromClasspathAsNode() {
    new HandlerTemplate<ArrayNode>(new TypeReference<ArrayNode>(){}){
      @Override
      public Reader getReader() throws IOException {
        return new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("string-array.json"));
      }

      @Override
      public void handle(ArrayNode values) {
        assertThat(values, equalTo(arrayNode("one", "two", "three")));
      }
    };
  }
  
  @Test
  public void handleResourceFromClasspathRelativeToClassAsList() {
    try( Reader in = new InputStreamReader(this.getClass().getResourceAsStream("string-array.json")) ) {
      List<String> values = mapper.readValue(in, new TypeReference<List<String>>(){});
      assertThat(values, equalTo(newArrayList("one", "two", "three")));
    } catch( IOException ioe ) {
      throw new UncheckedIOException("could not handle resource.", ioe);
    }
  }
  @Test
  public void handleResourceFromClasspathRelativeToClassAsNode() {
    try( Reader in = new InputStreamReader(this.getClass().getResourceAsStream("string-array.json")) ) {
      ArrayNode values = mapper.readValue(in, new TypeReference<ArrayNode>(){});
      assertThat(values, equalTo(arrayNode("one", "two", "three")));
    } catch( IOException ioe ) {
      throw new UncheckedIOException("could not handle resource.", ioe);
    }
  }

  @Test
  public void handleResourceFromStringAsList() {
    try( Reader in = new StringReader("[\"one\", \"two\", \"three\"]") ) {
      List<String> values = mapper.readValue(in, new TypeReference<List<String>>(){});
      assertThat(values, equalTo(newArrayList("one", "two", "three")));
    } catch( IOException ioe ) {
      throw new UncheckedIOException("could not handle resource.", ioe);
    }
  }

  @Test
  public void handleResourceFromStringAsNode() {
    try( Reader in = new StringReader("[\"one\", \"two\", \"three\"]") ) {
      ArrayNode values = mapper.readValue(in, new TypeReference<ArrayNode>(){});
      assertThat(values, equalTo(arrayNode("one", "two", "three")));
    } catch( IOException ioe ) {
      throw new UncheckedIOException("could not handle resource.", ioe);
    }
  }
  
  private static ArrayNode arrayNode( String... values) {
    ArrayNode result = JsonNodeFactory.instance.arrayNode();
    Arrays.asList(values).stream().forEach(result::add);
    return result;
  }
}
