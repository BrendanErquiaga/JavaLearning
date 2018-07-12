import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericsTest {


    @Test
    public void withoutGenerics() {
        List list = new ArrayList();
        list.add("one");

        String one = (String)list.get(0);
    }

    @Test
    public void withGenerics() {
        List<String> stringList = new ArrayList<>();
        stringList.add("one");

        String one = stringList.get(0);
    }

    @Test
    public void recoverInfo() {
        TypeReference<String> ref = new TypeReference<String>() {};
        TypeResolver resolver = new TypeResolver();
        ResolvedType type = resolver.resolve(ref.getClass());

        List<ResolvedType> mapParams = type.typeParametersFor(TypeReference.class);

        System.out.println(mapParams.get(0).toString());
    }

    @Test
    public void testRecursiveBuilder(){
        ChildRecursive childRecursive = new ChildRecursive().withValue1("one").withValue2("two");
    }

    @Test
    public void recoverTypeInfo() {
        TypeResolver resolver = new TypeResolver();
        ResolvedType type = resolver.resolve(StringIntMap.class);

        List<ResolvedType> mapParams = type.typeParametersFor(Map.class);
        System.out.println(mapParams.get(0).toString());
        System.out.println(mapParams.get(1).toString());
    }

    public class StringIntMap extends HashMap<String, Integer> {}

    public static class Recursive <T extends Recursive<T>> {
        String value1;

        public T withValue1(String value1) {
            this.value1 = value1;

            return (T)this;
        }
    }

    public static class ChildRecursive extends Recursive <ChildRecursive> {
        String value2;

        public ChildRecursive withValue2(String value2) {
            this.value2 = value2;

            return this;
        }

        public <T extends ChildRecursive & Comparable<ChildRecursive>> T getComparable() {
            return null;
        }
    }

}
