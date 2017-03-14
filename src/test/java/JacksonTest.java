import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class JacksonTest {

    public static ObjectMapper mapper = new ObjectMapper();

    public static class Data {
        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public Data withValue(int value){
            this.value = value;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return value == data.value;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @JsonProperty(value = "Number")
        private int value;
    }

    @Test
    public void dataTest() throws Exception {
        Data data = new Data().withValue(5);

        ObjectNode json = mapper.convertValue(data, ObjectNode.class);

        System.out.println(mapper.writeValueAsString(data));

        assertThat(json.get("Number").asInt(), equalTo(5));
    }

    @Test
    public void secondDataTest() throws Exception {

    }
}
