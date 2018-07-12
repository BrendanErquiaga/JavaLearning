import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommandPatternTest {

    public static class ComposedRunnable implements Runnable {
        List<Runnable> children = new ArrayList<>();

        public ComposedRunnable addChild(Runnable child){
            children.add(child);
            return this;
        }

        public void run() {
            children.forEach(Runnable::run);
        }
    }

    @Test
    public void stuff(){
        Runnable runnableOne = ()-> {System.out.println("Hello");};
        Runnable runnableTwo = ()-> {System.out.println("Goodbye");};

        ComposedRunnable composedRunnable = new ComposedRunnable().addChild(runnableOne).addChild(runnableTwo);

        composedRunnable.run();
    }

    public class Chain<T> implements Predicate<T> {
        List<Predicate<T>> children = new ArrayList<>();

        public Chain<T> addChild(Predicate<T> child){
            children.add(child);
            return this;
        }

        @Override
        public boolean test(T t) {
            return children.stream().map(c->c.test(t)).filter(b->b).findFirst().orElse(Boolean.FALSE);
        }
    }

    private static Predicate<ObjectNode> handleType(String type, Consumer<ObjectNode> consumer){
        return objectNode -> {
            JsonNode jsonNodeType = objectNode.get("type");

            if(jsonNodeType.isTextual() && jsonNodeType.asText().equals(type)){
                consumer.accept(objectNode);
                return true;
            }

            return false;
        };
    }

    private static ObjectNode event(String type){
        return JsonNodeFactory.instance.objectNode().put("type", type);
    }

    @Test
    public void chainTest(){
        Chain<ObjectNode> chain = new Chain<>();

        chain.addChild(handleType("hello", node -> System.out.println("Hello")))
             .addChild(handleType("goodbye", node -> System.out.println("Goodbye")))
             .addChild(handleType("hello", node -> System.out.println("Second Hello")));

        ObjectNode node = JsonNodeFactory.instance.objectNode().put("type", "goodbye");

        //chain.test(node);

        Chain<ObjectNode> secondChain = new Chain<>();
        secondChain.addChild(handleType("hola", secondNode -> System.out.println("Hola")))
                .addChild(handleType("adios", secondNode -> System.out.println("Adios")));

        Chain<ObjectNode> parentChain = new Chain<ObjectNode>().addChild(chain).addChild(secondChain);

        parentChain.test(event("hello"));
        parentChain.test(event("hola"));
    }
}
