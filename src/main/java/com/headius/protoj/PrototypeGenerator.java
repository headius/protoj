package com.headius.protoj;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JiteClass;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static me.qmx.jitescript.util.CodegenUtils.*;

public class PrototypeGenerator {
    private final Cache<String, Class> prototypes = CacheBuilder.newBuilder().weakValues().build();
    private static final MessageDigest SHA1;
    private final ClassLoader parentClassLoader;

    public PrototypeGenerator(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    static {
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SHA1 = sha1;
    }

    private static MessageDigest sha1() {
        try {
            return (MessageDigest) SHA1.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }

    public Class generate(String... baseProps) {
        return generate(null, new String[0], baseProps);
    }

    public Class generate(Prototype prototype, String[] baseProps, final String... modifications) {
        String[] newProps;
        if (baseProps.length == 0) {
            if (modifications.length == 0) {
                return Prototype.class;
            }
            newProps = modifications;
        } else {
            Set<String> combined = new HashSet<String>();
            combined.addAll(Arrays.asList(baseProps));
            combined.addAll(Arrays.asList(modifications));
            newProps = combined.toArray(new String[combined.size()]);
        }

        Arrays.sort(newProps);
        Class p = protoClassFromProps(newProps);

        try {
            if (p == null) {
                // create a new one
                Class _base = protoClassFromProps(newProps);
                if (_base == null) {
                    if (modifications.length != 0) {
                        // recurse and generate the base first
                        _base = generate(baseProps);
                    } else {
                        // all modifications, use Prototype as base
                        _base = Prototype.class;
                    }
                }
                final Class base = _base;
                final String hash = hashFromStrings(newProps);
                final String[] newFields = newProps;

                JiteClass jiteClass = new JiteClass(hash, p(base), new String[0]) {{
                    // no-arg constructor for empty instance
                    defineDefaultConstructor();

                    // parent class constructor
                    defineMethod("<init>", ACC_PUBLIC, sig(void.class, base), new CodeBlock() {{
                        aload(0);
                        aload(1);
                        invokespecial(p(base), "<init>", sig(void.class, base));
                        voidreturn();
                    }});

                    // copy constructor
                    defineMethod("<init>", ACC_PUBLIC, sig(void.class, "L" + hash + ";"), new CodeBlock() {{
                        aload(0);
                        aload(1);
                        invokespecial(p(base), "<init>", sig(void.class, base));

                        for (String prop : modifications) {
                            aload(0);
                            aload(1);
                            getfield(hash, prop, ci(Object.class));
                            putfield(hash, prop, ci(Object.class));
                        }

                        voidreturn();
                    }});

                    // in-order values array constructor
                    defineMethod("<init>", ACC_PUBLIC, sig(void.class, Object[].class), new CodeBlock() {{
                        aload(0);
                        invokespecial(p(base), "<init>", sig(void.class));

                        int i = 0;
                        for (String prop : newFields) {
                            aload(0);
                            aload(1);
                            pushInt(i);
                            aaload();
                            putfield(hash, prop, ci(Object.class));
                            i++;
                        }

                        voidreturn();
                    }});

                    // in-order values arguments constructor
                    defineMethod("<init>", ACC_PUBLIC, sig(void.class, params(Object.class, newFields.length)), new CodeBlock() {{
                        aload(0);
                        invokespecial(p(base), "<init>", sig(void.class));

                        int i = 0;
                        for (String prop : newFields) {
                            aload(0);
                            aload(i + 1);
                            putfield(hash, prop, ci(Object.class));
                            i++;
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

                p = defineClass(prototype, jiteClass);
                prototypes.put(hash, p);
            }

            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class defineClass(Prototype prototype, JiteClass jiteClass) {
        Class p;
        if (prototype != null) {
            p = new DynamicClassLoader(prototype.getClass().getClassLoader()).define(jiteClass);
        } else {
            p = new DynamicClassLoader(this.parentClassLoader).define(jiteClass);
        }
        return p;
    }

    public Prototype construct(String[] keys, Object[] values) {
        try {
            Class p = generate(keys);
            return (Prototype) p.getConstructor(Object[].class).newInstance((Object) values);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Prototype construct(Prototype base, String... modifications) {
        try {
            Class p = generate(base, base.properties(), modifications);
            return (Prototype) p.getConstructor(params(base.getClass())).newInstance(base);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Prototype construct(String key0, Object value0) {
        try {
            Class p = protoClassFromProps(key0);
            return (Prototype) p.getConstructor(params(Object.class, 1)).newInstance(value0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Prototype construct(String key0, String key1, Object value0, Object value1) {
        try {
            Class p = generate(key0, key1);
            return (Prototype) p.getConstructor(params(Object.class, 2)).newInstance(value0, value1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Prototype construct(String key0, String key1, String key2, Object value0, Object value1, Object value2) {
        try {
            Class p = generate(key0, key1, key2);
            return (Prototype) p.getConstructor(params(Object.class, 3)).newInstance(value0, value1, value2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class protoClassFromProps(String... newProps) {
        final String hash = hashFromStrings(newProps);
        return prototypes.getIfPresent(hash);
    }

    public String hashFromStrings(String... strings) {
        MessageDigest sha1 = sha1();
        sha1.update(Arrays.toString(strings).getBytes());
        byte[] digest = sha1.digest();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            builder.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return builder.toString().toUpperCase(Locale.ENGLISH);
    }

    public static class DynamicClassLoader extends ClassLoader {

        public DynamicClassLoader() {
        }

        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> define(JiteClass jiteClass) {
            byte[] classBytes = jiteClass.toBytes();
            return super.defineClass(jiteClass.getClassName(), classBytes, 0, classBytes.length);
        }
    }
}
