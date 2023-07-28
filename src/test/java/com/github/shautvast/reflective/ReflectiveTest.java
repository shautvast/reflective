package com.github.shautvast.reflective;

import com.github.shautvast.rusty.Panic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectiveTest {

    @Test
    void testMethods() {
        Dummy dummy = new Dummy("bar");
        MetaClass metaDummy = Reflective.getMetaForClass(dummy.getClass());
        assertEquals("com.github.shautvast.reflective.ReflectiveTest$Dummy", metaDummy.getName());

        Iterator<MetaField> fields = metaDummy.getFields().iterator();
        assertTrue(fields.hasNext());
        assertEquals("name", fields.next().getName());

        Set<MetaMethod> methods = metaDummy.getMethods();
        assertFalse(methods.isEmpty());
        assertEquals(4, methods.size());

        MetaMethod equals = metaDummy.getMethod("equals").orElseGet(Assertions::fail);
        assertEquals(List.of(Object.class), equals.getParameters());
        assertEquals(boolean.class, equals.getReturnParameter());
        assertTrue(Modifier.isPublic(equals.getModifiers()));

        MetaMethod hashCode = metaDummy.getMethod("hashCode").orElseGet(Assertions::fail);
        assertEquals(List.of(), hashCode.getParameters());
        assertEquals(int.class, hashCode.getReturnParameter());
        assertTrue(Modifier.isPublic(hashCode.getModifiers()));

        MetaMethod getName = metaDummy.getMethod("getName").orElseGet(Assertions::fail);
        assertEquals(List.of(), getName.getParameters());
        assertEquals(String.class, getName.getReturnParameter());
        assertTrue(Modifier.isPublic(getName.getModifiers()));

        MetaMethod privateMethod = metaDummy.getMethod("privateMethod").orElseGet(Assertions::fail);
        assertEquals(List.of(), privateMethod.getParameters());
        assertEquals(String[].class, privateMethod.getReturnParameter());
        assertTrue(Modifier.isPrivate(privateMethod.getModifiers()));
    }


    @Test
    void testInvokeGetter() {
        Dummy dummy = new Dummy("bar");
        MetaMethod getName = Reflective.getMetaForClass(dummy.getClass()).getMethod("getName").orElseGet(Assertions::fail);

        assertThrows(Panic.class, () -> getName.invoke("foo").unwrap());
        assertEquals("bar", getName.invoke(dummy).unwrap());
    }

    @Test
    void testInvokeSetter() {
        Dummy dummy = new Dummy("bar");
        MetaClass metaForClass = Reflective.getMetaForClass(dummy.getClass());
        MetaMethod setName = metaForClass.getMethod("setName").orElseGet(Assertions::fail);

        assertEquals("bar", dummy.getName()); // before invoke
        setName.invoke(dummy, "foo");
        assertEquals("foo", dummy.getName()); // after invoke
    }


    public static class Dummy {
        private String name;

        public Dummy(String name) {
            this.name = name;
        }

        public String getName() {
            return privateMethod()[0];
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Dummy;
        }

        @Override
        public int hashCode() {
            return 6;
        }

        private String[] privateMethod() {
            return new String[]{name, "bar"};
        }
    }
}