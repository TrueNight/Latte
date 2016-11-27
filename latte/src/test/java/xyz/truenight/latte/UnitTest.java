package xyz.truenight.latte;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class UnitTest {

    static {
        Latte.setDebug(true);
    }

    @Test
    public void equalTest() throws Exception {
        TestObject hello = getTestObject();
        TestObject hero = getTestObject();

        assertTrue(Latte.equal(hello, hero));

        hero = getTestObject2();
        assertFalse(Latte.equal(hello, hero));

        Object mike = new Object();
        Object like = new TestObject();

        assertFalse(Latte.equal(mike, like));
    }

    @Test
    public void cloneTest() throws Exception {
        TestObject one = getTestObject();
        TestObject two = Latte.clone(one);

        assertFalse(one == two);
        assertFalse(Latte.equal(one, two));

        two.should = new Object();
        assertTrue(Latte.equal(one, two));
    }

    @Test
    public void useAdapterTest() throws Exception {
        Throwable origin = new Throwable("Ex");
        Throwable th = Latte.clone(origin); // handle recursive reference

        assertFalse(origin == th);
        assertTrue(Latte.equal(origin, th));
        System.out.println(th.toString());
    }

    @Test
    public void generalizeTest() throws Exception {
        TestObject one = getTestObject();
        TestObject two = getTestObject3();

        assertTrue(Latte.equal(one, two));

        two = getTestObject4();

        assertTrue(Latte.equal(one, two));
        assertTrue(Latte.equal(one.die, two.die));
        assertTrue(Latte.equal(two.die, one.die));
    }

    @Test
    public void recursiveRefTest() throws Exception {
        Throwable origin = new Throwable("Ex");
        Throwable th = Latte.clone(origin); // handle recursive reference

        assertFalse(origin == th);
        assertTrue(Latte.equal(origin, th));
        System.out.println(th.toString());
    }

    private TestObject getTestObject() {
        TestObject hello;
        hello = new TestObject();
        hello.something = 10;
        List<TestObject> what = new ArrayList<>();
        what.add(new TestObject(1));
        what.add(new TestObject(2));
        what.add(new TestObject(3));
        what.add(new TestObject(4));
        what.add(new TestObject(5));
        Collections.shuffle(what);
        hello.what = what;
        hello.you = new ArrayList<>();
        hello.you.add(new Throwable("Hello"));
        hello.you.add(new Throwable("Unnamed"));
        hello.you.add(new Throwable("Hero"));
        hello.should = new Object();
        hello.know = 10L;
        hello.heroes = 10.0D;
        hello.die = new HashMap<>();
        hello.die.put("one", new TestObject(1));
        hello.die.put("two", new TestObject(2));
        hello.die.put("three", new TestObject(3));
        hello.die.put("four", new TestObject(4));
        hello.die.put("five", new TestObject(5));
        return hello;
    }

    private TestObject getTestObject2() {
        TestObject hello = getTestObject();
        List<Object> should = new ArrayList<>();
        should.add(new TestObject(1));
        should.add(new TestObject(2));
        should.add(new TestObject(3));
        should.add(new TestObject(4));
        should.add(new TestObject(5));
        hello.should = should;
        hello.never = getTestObject();
        return hello;
    }

    private TestObject getTestObject3() {
        TestObject hello = getTestObject();
        hello.what = new HashSet<>();
        hello.what.add(new TestObject(1));
        hello.what.add(new TestObject(2));
        hello.what.add(new TestObject(3));
        hello.what.add(new TestObject(4));
        hello.what.add(new TestObject(5));
        return hello;
    }

    private TestObject getTestObject4() {
        TestObject hello = getTestObject();
        hello.die = new LinkedTreeMap<>();
        hello.die.put("one", new Test2Object(1));
        hello.die.put("two", new Test2Object(2));
        hello.die.put("three", new Test2Object(3));
        hello.die.put("four", new Test2Object(4));
        hello.die.put("five", new Test2Object(5));
        return hello;
    }

    public class TestObject {
        private int something;

        @UnorderedCollection // element order will be ignored during comparison
                Collection<TestObject> what; // aggregation of same type items

        Collection<Throwable> you;

        @IgnoreField(ignoreEqual = false) // field will be compared but won't be cloned
        private Object should;

        @IgnoreField
        private Long know; // will be ignored

        transient double heroes; // also ignored

        @UseAdapter(CustomAdapter.class)
        TestObject never; // also ignored

        Map<Object, Object> die; // also ignored

        public TestObject() {
        }

        public TestObject(int something) {
            this.something = something;
        }
    }

    class Test2Object extends TestObject {
        int blahblah;

        public Test2Object() {
            this.blahblah = 12;
        }

        public Test2Object(int something) {
            super(something);
            this.blahblah = 10;
        }
    }

    public class CustomAdapter implements TypeAdapter<TestObject> {

        @Override
        public boolean equal(TestObject a, TestObject b) {
            return a == b;
        }

        @Override
        public TestObject clone(TestObject value) {
            return value;
        }
    }
}