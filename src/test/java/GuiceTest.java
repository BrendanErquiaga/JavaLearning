import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class GuiceTest {

    public static class Simple {
        protected double random = Math.random();

        public double getRandom() { return  random; }
    }

    @Singleton
    public static class SimpleSingleton {
        protected double random = Math.random();

        public double getRandom() { return  random; }
    }

    Injector injector;

    @Before
    public void setup() {
        injector = Guice.createInjector();
    }

    @Test
    public void simpleGuice() {
        Simple simple = injector.getInstance(Simple.class);
        Simple simpleTwo = injector.getInstance(Simple.class);

        assertThat(simple.getRandom(), not(equalTo(simpleTwo.getRandom())));
    }

    @Test
    public void simpleSingletonGuice() {
        SimpleSingleton simple = injector.getInstance(SimpleSingleton.class);
        SimpleSingleton simpleTwo = injector.getInstance(SimpleSingleton.class);

        assertThat(simple.getRandom(), equalTo(simpleTwo.getRandom()));
    }

    @Singleton
    public static class SimpleSingletonA {
        @Inject
        public SimpleSingletonB b;
    }

    @Singleton
    public static class SimpleSingletonB {
        @Inject
        public SimpleSingletonA a;
    }

    @Test
    public void aAttachedToB() {
        SimpleSingletonA a = injector.getInstance(SimpleSingletonA.class);
        SimpleSingletonB b = injector.getInstance(SimpleSingletonB.class);

        assertThat(a.b, equalTo(b));
    }

    public static interface WhatSingletonAIf {
        public double getRandom();

        public WhatSingletonBIf getB();
    }

    @Singleton
    public static class WhatSingletonA implements WhatSingletonAIf{


        protected double random = Math.random();

        public double getRandom() { return  random; }

        public WhatSingletonBIf b;

        public WhatSingletonBIf getB(){
            return b;
        }

        @Inject
        public WhatSingletonA(WhatSingletonBIf b) {
            this.b = b;
        }
    }

    public static interface WhatSingletonBIf {
        public double getRandom();

        public WhatSingletonAIf getA();
    }

    @Singleton
    public static class WhatSingletonB implements WhatSingletonBIf {

        protected double random = Math.random();

        public double getRandom() { return  random; }

        public WhatSingletonAIf a;

        public WhatSingletonAIf getA(){
            return a;
        };

        @Inject
        public WhatSingletonB(WhatSingletonAIf a) {
            this.a = a;
        }
    }

    @Test
    public void aAttachedToBWhat() {
        injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                bind(WhatSingletonAIf.class).to(WhatSingletonA.class);
                bind(WhatSingletonBIf.class).to(WhatSingletonB.class);
            }
        });
        WhatSingletonAIf a = injector.getInstance(WhatSingletonAIf.class);
        WhatSingletonBIf b = injector.getInstance(WhatSingletonBIf.class);

        assertThat(a.getB().getRandom(), equalTo(b.getRandom()));

        System.out.println(a.getClass().getName());
        System.out.println(b.getClass().getName());
    }

    public static class OurClass {
        @Inject
        @Named("A")
        public TheirClass a;

        @Inject
        @Named("B")
        public TheirClass b;
    }

    public static class TheirClass {
        public int configNumber;

        public TheirClass(int configNumber){
            this.configNumber = configNumber;
        }
    }

    @Test
    public void oursTheirs() {
        injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
            }

            @Provides
            @Singleton
            @Named("A")
            public TheirClass provideA(){
                return new TheirClass(1);
            }

            @Provides
            @Singleton
            @Named("B")
            public TheirClass provideB(){
                return new TheirClass(2);
            }
        });

        OurClass ourClass = new OurClass();

        assertThat(ourClass.a, nullValue());
        assertThat(ourClass.b, nullValue());

        injector.injectMembers(ourClass);

        assertThat(ourClass.a.configNumber, equalTo(1));
        assertThat(ourClass.b.configNumber, equalTo(2));
    }
}
