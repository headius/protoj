package com.headius.protoj;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PrototypeGeneratorTest {
    @org.junit.Test
    public void testGenerate() throws Throwable {
        Prototype base = new Prototype(null);

        Prototype withFoo = PrototypeGenerator.generate(base, "foo");

        assertArrayEquals("withFoo fields were not ['foo']", new String[]{"foo"}, withFoo.properties());

        Field foo = withFoo.getClass().getDeclaredField("foo");

        assertEquals("foo field was not an Object", Object.class, foo.getType());

        MethodHandle fooGetter = MethodHandles.lookup().unreflectGetter(foo);
        MethodHandle fooSetter = MethodHandles.lookup().unreflectSetter(foo);

        fooSetter.invokeWithArguments(withFoo, "blah");

        assertEquals("handle set of foo did not set it to 'blah'", "blah", fooGetter.invokeWithArguments(withFoo));
    }
}
