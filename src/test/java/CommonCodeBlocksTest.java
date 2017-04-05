import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;

import static org.hamcrest.Matchers.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.*;

/**
 * A set of tests where the logic binds JSON from some source and then handles that binding,
 * with some light error handling.  There is variance both in the sourcing of data and in
 * the type it is bound to.  We can apply some different patterns to this and see if
 * we can make the logic more clear.
 * 
 * @author Christian Trimble
 *
 */
public class CommonCodeBlocksTest {
  
  ObjectMapper mapper = new ObjectMapper();
  
  @Test
  public void handleResourceFromClasspathAsList() {
    try( Reader in = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("string-array.json")) ) {
      List<String> values = mapper.readValue(in, new TypeReference<List<String>>(){});
      assertThat(values, equalTo(newArrayList("one", "two", "three")));
    } catch( IOException ioe ) {
      throw new UncheckedIOException("could not handle resource.", ioe);
    }
  }
  
  @Test
  public void handleResourceFromClasspathAsNode() {
    try( Reader in = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("string-array.json")) ) {
      ArrayNode values = mapper.readValue(in, new TypeReference<ArrayNode>(){});
      assertThat(values, equalTo(arrayNode("one", "two", "three")));
    } catch( IOException ioe ) {
      throw new UncheckedIOException("could not handle resource.", ioe);
    }
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
