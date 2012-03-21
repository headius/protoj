package com.headius.protoj;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JiteClass;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static me.qmx.jitescript.util.CodegenUtils.*;

public class PrototypeGenerator {
    private static final Cache<String, Class> prototypes = CacheBuilder.newBuilder().weakValues().build();
    private static final MessageDigest SHA1;
    static {
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        SHA1 = sha1;
    }

    private static MessageDigest sha1() {
        try {
            return (MessageDigest)SHA1.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }

    public static Prototype generate(final Prototype base, final String... modifications) {
        final String[] baseProps = base.properties();
        String[] newProps;
        if (baseProps.length == 0) {
            newProps = modifications;
        } else {
            List<String> combined = Arrays.asList(baseProps);
            combined.addAll(Arrays.asList(modifications));
            newProps = combined.toArray(new String[combined.size()]);
        }
        Arrays.sort(newProps);
        final String hash = getHashForStrings(newProps);
        
        // look for existing prototype
        Class p = prototypes.getIfPresent(hash);

        try {
            if (p == null) {
                // create a new one
                final String[] newFields = newProps;
                JiteClass jiteClass = new JiteClass(hash, p(base.getClass()), new String[0]) {{
                    // parent class constructor
                    defineMethod("<init>", ACC_PUBLIC, sig(void.class, base.getClass()), new CodeBlock() {{
                        aload(0);
                        aload(1);
                        invokespecial(p(base.getClass()), "<init>", sig(void.class, base.getClass()));
                        voidreturn();
                    }});

                    // copy constructor
                    defineMethod("<init>", ACC_PUBLIC, sig(void.class, "L" + hash + ";"), new CodeBlock() {{
                        aload(0);
                        aload(1);
                        invokespecial(p(base.getClass()), "<init>", sig(void.class, base.getClass()));

                        for (String prop : modifications) {
                            aload(0);
                            aload(1);
                            getfield(hash, prop, ci(Object.class));
                            putfield(hash, prop, ci(Object.class));
                        }

                        voidreturn();
                    }});

                    // properties method
                    defineMethod("properties", ACC_PUBLIC, sig(String[].class), new CodeBlock() {{
                        pushInt(newFields.length);
                        anewarray(p(String.class));
                        for (int i = 0; i < newFields.length; i++) {
                            dup();
                            pushInt(i);
                            ldc(newFields[i]);
                            aastore();
                        }
                        areturn();
                    }});

                    // fields
                    for (String prop : modifications) {
                        defineField(prop, ACC_PUBLIC, ci(Object.class), null);
                    }
                }};

                p = new DynamicClassLoader().define(jiteClass);
                prototypes.put(hash, p);
            }

            return (Prototype)p.getConstructor(base.getClass()).newInstance(base);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHashForStrings(String[] strings) {
        MessageDigest sha1 = sha1();
        sha1.update(Arrays.toString(strings).getBytes());
        byte[] digest = sha1.digest();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            builder.append(Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 ));
        }
        return builder.toString().toUpperCase(Locale.ENGLISH);
    }

    public static class DynamicClassLoader extends ClassLoader {
        public Class<?> define(JiteClass jiteClass) {
            byte[] classBytes = jiteClass.toBytes();
            return super.defineClass(jiteClass.getClassName(), classBytes, 0, classBytes.length);
        }
    }
}
