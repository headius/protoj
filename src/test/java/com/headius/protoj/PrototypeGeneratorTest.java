package com.headius.protoj;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PrototypeGeneratorTest {

    private static PrototypeGenerator prototypeGenerator = new PrototypeGenerator(new PrototypeGenerator.DynamicClassLoader());

    @org.junit.Test
    public void testConstructFromBase() throws Throwable {
        Prototype base = new Prototype(null);

        Prototype withFoo = prototypeGenerator.construct(base, "foo");

        assertArrayEquals("withFoo fields were not ['foo']", new String[]{"foo"}, withFoo.properties());

        Field foo = withFoo.getClass().getDeclaredField("foo");

        assertEquals("foo field was not an Object", Object.class, foo.getType());

        MethodHandle fooGetter = MethodHandles.lookup().unreflectGetter(foo);
        MethodHandle fooSetter = MethodHandles.lookup().unreflectSetter(foo);

        fooSetter.invokeWithArguments(withFoo, "blah");

        assertEquals("handle set of foo did not set it to 'blah'", "blah", fooGetter.invokeWithArguments(withFoo));
    }

    @org.junit.Test
    public void testConstructFromKeyValue() throws Throwable {
        Prototype withFoo = prototypeGenerator.construct("foo", "blah");

        assertArrayEquals("withFoo fields were not ['foo']", new String[]{"foo"}, withFoo.properties());

        Field foo = withFoo.getClass().getDeclaredField("foo");

        assertEquals("foo field was not an Object", Object.class, foo.getType());

        MethodHandle fooGetter = MethodHandles.lookup().unreflectGetter(foo);

        assertEquals("prototype construct did not set foo 'blah'", "blah", fooGetter.invokeWithArguments(withFoo));
    }

    @org.junit.Test
    public void testConstructFromKey2Value2() throws Throwable {
        Prototype withFooQuux = prototypeGenerator.construct("foo", "quux", "blah", "yummy");

        assertArrayEquals("withFoo fields were not ['foo', 'quux']", new String[]{"foo", "quux"}, withFooQuux.properties());

        Field foo = withFooQuux.getClass().getDeclaredField("foo");
        Field quux = withFooQuux.getClass().getDeclaredField("quux");

        assertEquals("foo field was not an Object", Object.class, foo.getType());
        assertEquals("quux field was not an Object", Object.class, quux.getType());

        MethodHandle fooGetter = MethodHandles.lookup().unreflectGetter(foo);
        MethodHandle quuxGetter = MethodHandles.lookup().unreflectGetter(quux);

        assertEquals("prototype construct did not set foo 'blah'", "blah", fooGetter.invokeWithArguments(withFooQuux));
        assertEquals("prototype construct did not set quux 'yummy'", "yummy", quuxGetter.invokeWithArguments(withFooQuux));
    }

    @org.junit.Test
    public void testConstructFromKey3Value3() throws Throwable {
        Prototype withFooQuuxZaj = prototypeGenerator.construct("foo", "quux", "zaj", "blah", "yummy", "piff");

        assertArrayEquals("withFoo fields were not ['foo', 'quux', 'zaj']", new String[]{"foo", "quux", "zaj"}, withFooQuuxZaj.properties());

        Field foo = withFooQuuxZaj.getClass().getDeclaredField("foo");
        Field quux = withFooQuuxZaj.getClass().getDeclaredField("quux");
        Field zaj = withFooQuuxZaj.getClass().getDeclaredField("zaj");

        assertEquals("foo field was not an Object", Object.class, foo.getType());
        assertEquals("quux field was not an Object", Object.class, quux.getType());
        assertEquals("zaj field was not an Object", Object.class, zaj.getType());

        MethodHandle fooGetter = MethodHandles.lookup().unreflectGetter(foo);
        MethodHandle quuxGetter = MethodHandles.lookup().unreflectGetter(quux);
        MethodHandle zajGetter = MethodHandles.lookup().unreflectGetter(zaj);

        assertEquals("prototype construct did not set foo 'blah'", "blah", fooGetter.invokeWithArguments(withFooQuuxZaj));
        assertEquals("prototype construct did not set quux 'yummy'", "yummy", quuxGetter.invokeWithArguments(withFooQuuxZaj));
        assertEquals("prototype construct did not set zaj 'piff'", "piff", zajGetter.invokeWithArguments(withFooQuuxZaj));
    }

    @org.junit.Test
    public void testConstructFromKeyAryValueAry() throws Throwable {
        Prototype withFooQuuxZaj = prototypeGenerator.construct(new String[]{"foo", "quux", "zaj"}, new Object[]{"blah", "yummy", "piff"});

        assertArrayEquals("withFoo fields were not ['foo', 'quux', 'zaj']", new String[]{"foo", "quux", "zaj"}, withFooQuuxZaj.properties());

        Field foo = withFooQuuxZaj.getClass().getDeclaredField("foo");
        Field quux = withFooQuuxZaj.getClass().getDeclaredField("quux");
        Field zaj = withFooQuuxZaj.getClass().getDeclaredField("zaj");

        assertEquals("foo field was not an Object", Object.class, foo.getType());
        assertEquals("quux field was not an Object", Object.class, quux.getType());
        assertEquals("zaj field was not an Object", Object.class, zaj.getType());

        MethodHandle fooGetter = MethodHandles.lookup().unreflectGetter(foo);
        MethodHandle quuxGetter = MethodHandles.lookup().unreflectGetter(quux);
        MethodHandle zajGetter = MethodHandles.lookup().unreflectGetter(zaj);

        assertEquals("prototype construct did not set foo 'blah'", "blah", fooGetter.invokeWithArguments(withFooQuuxZaj));
        assertEquals("prototype construct did not set quux 'yummy'", "yummy", quuxGetter.invokeWithArguments(withFooQuuxZaj));
        assertEquals("prototype construct did not set zaj 'piff'", "piff", zajGetter.invokeWithArguments(withFooQuuxZaj));
    }

    @Test
    public void testPrototypalInheritance() throws Throwable, IllegalAccessException {
        final Prototype prototype = prototypeGenerator.construct(new Prototype(null), "test", "lol");
        final Field prototypeTest = prototype.getClass().getDeclaredField("test");
        final MethodHandle testSetter = MethodHandles.lookup().unreflectSetter(prototypeTest);
        testSetter.invokeWithArguments(prototype, "prototypeValue");
        final Prototype child = prototypeGenerator.construct(prototype, "test2", "test3");
        final Field test = child.getClass().getField("test");
        final MethodHandle testGetter = MethodHandles.lookup().unreflectGetter(test);
        assertEquals("prototype construct did not set test 'prototypeValue'", "prototypeValue", testGetter.invokeWithArguments(child));
    }
}
