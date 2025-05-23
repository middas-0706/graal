/*
 * Copyright (c) 2015, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.api.test.profiles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.oracle.truffle.api.dsl.InlineSupport.InlinableField;
import com.oracle.truffle.api.profiles.ByteValueProfile;
import com.oracle.truffle.api.profiles.InlinedExactClassProfile;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.tck.tests.TruffleTestAssumptions;

public class ExactClassValueProfileTest extends AbstractProfileTest {

    private static Object[] VALUES = new Object[]{"", 42, 42d, new TestBaseClass(), new TestSubClass()};

    private static class TestBaseClass {
    }

    private static final class TestSubClass extends TestBaseClass {
    }

    @BeforeClass
    public static void runWithWeakEncapsulationOnly() {
        TruffleTestAssumptions.assumeWeakEncapsulation();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T createEnabled(Class<T> p) {
        if (p == ValueProfile.class) {
            for (Class<?> c : ValueProfile.class.getDeclaredClasses()) {
                if (c.getSimpleName().equals("ExactClass")) {
                    return (T) super.createEnabled(c);
                }
            }
        }
        return super.createEnabled(p);
    }

    @Override
    protected InlinableField[] getInlinedFields() {
        return createInlinedFields(1, 0, 0, 0, 1);
    }

    @Test
    public void testNotCrashing() {
        ValueProfile profile = createEnabled(ValueProfile.class);
        profile.disable();
        profile.reset();
        assertEquals(profile, profile);
        assertEquals(profile.hashCode(), profile.hashCode());
        assertNotNull(profile.toString());
    }

    @Test
    public void testInitial() {
        ValueProfile profile = createEnabled(ValueProfile.class);
        assertThat(isGeneric(profile), is(false));
        assertThat(isUninitialized(profile), is(true));
        profile.toString(); // test that it is not crashing
    }

    @Test
    public void testProfileOneObject() {
        for (Object value : VALUES) {
            ValueProfile profile = createEnabled(ValueProfile.class);
            Object result = profile.profile(value);
            assertThat(result, is(value));
            assertEquals(getCachedValue(profile), value.getClass());
            assertThat(isUninitialized(profile), is(false));
        }
    }

    @Test
    public void testProfileTwoObject() {
        for (Object value0 : VALUES) {
            for (Object value1 : VALUES) {
                ValueProfile profile = createEnabled(ValueProfile.class);
                Object result0 = profile.profile(value0);
                Object result1 = profile.profile(value1);

                assertThat(result0, is(value0));
                assertThat(result1, is(value1));

                if (value0 == value1) {
                    assertThat(getCachedValue(profile), is((Object) value0.getClass()));
                    assertThat(isGeneric(profile), is(false));
                } else {
                    assertThat(isGeneric(profile), is(true));
                }
                assertThat(isUninitialized(profile), is(false));
                toString(profile); // test that it is not crashing
            }
        }
    }

    @Test
    public void testProfileThreeObject() {
        for (Object value0 : VALUES) {
            for (Object value1 : VALUES) {
                for (Object value2 : VALUES) {
                    ValueProfile profile = createEnabled(ValueProfile.class);
                    Object result0 = profile.profile(value0);
                    Object result1 = profile.profile(value1);
                    Object result2 = profile.profile(value2);

                    assertThat(result0, is(value0));
                    assertThat(result1, is(value1));
                    assertThat(result2, is(value2));

                    if (value0 == value1 && value1 == value2) {
                        assertThat(getCachedValue(profile), is((Object) value0.getClass()));
                        assertThat(isGeneric(profile), is(false));
                    } else {
                        assertThat(isGeneric(profile), is(true));
                    }
                    assertThat(isUninitialized(profile), is(false));
                    profile.toString(); // test that it is not crashing

                }
            }
        }
    }

    @Test
    public void testNotCrashingInlined() {
        InlinedExactClassProfile profile = createEnabled(InlinedExactClassProfile.class);
        profile.disable(state);
        profile.reset(state);
        assertEquals(profile, profile);
        assertEquals(profile.hashCode(), profile.hashCode());
        assertNotNull(profile.toString());
    }

    @Test
    public void testInitialInlined() {
        ByteValueProfile profile = createEnabled(ByteValueProfile.class);
        assertThat(isGeneric(profile), is(false));
        assertThat(isUninitialized(profile), is(true));
        profile.toString(); // test that it is not crashing
    }

    @Test
    public void testProfileOneObjectInlined() {
        for (Object value : VALUES) {
            InlinedExactClassProfile profile = createEnabled(InlinedExactClassProfile.class);
            Object result = profile.profile(state, value);
            assertThat(result, is(value));
            assertEquals(getCachedValue(profile), value.getClass());
            assertThat(isUninitialized(profile), is(false));
        }
    }

    @Test
    public void testProfileTwoObjectInlined() {
        for (Object value0 : VALUES) {
            for (Object value1 : VALUES) {
                InlinedExactClassProfile profile = createEnabled(InlinedExactClassProfile.class);
                Object result0 = profile.profile(state, value0);
                Object result1 = profile.profile(state, value1);

                assertThat(result0, is(value0));
                assertThat(result1, is(value1));

                if (value0 == value1) {
                    assertThat(getCachedValue(profile), is((Object) value0.getClass()));
                    assertThat(isGeneric(profile), is(false));
                } else {
                    assertThat(isGeneric(profile), is(true));
                }
                assertThat(isUninitialized(profile), is(false));
                toString(profile); // test that it is not crashing
            }
        }
    }

    @Test
    public void testProfileThreeObjectInlined() {
        for (Object value0 : VALUES) {
            for (Object value1 : VALUES) {
                for (Object value2 : VALUES) {
                    InlinedExactClassProfile profile = createEnabled(InlinedExactClassProfile.class);
                    Object result0 = profile.profile(state, value0);
                    Object result1 = profile.profile(state, value1);
                    Object result2 = profile.profile(state, value2);

                    assertThat(result0, is(value0));
                    assertThat(result1, is(value1));
                    assertThat(result2, is(value2));

                    if (value0 == value1 && value1 == value2) {
                        assertThat(getCachedValue(profile), is((Object) value0.getClass()));
                        assertThat(isGeneric(profile), is(false));
                    } else {
                        assertThat(isGeneric(profile), is(true));
                    }
                    assertThat(isUninitialized(profile), is(false));
                    profile.toString(); // test that it is not crashing

                }
            }
        }
    }

    @Test
    public void testDisabled() {
        ValueProfile p = ValueProfile.getUncached();
        for (Object value : VALUES) {
            assertThat(p.profile(value), is(value));
        }
        p.toString(); // test that it is not crashing
    }

    @Test
    public void testDisabledInlined() {
        InlinedExactClassProfile p = InlinedExactClassProfile.getUncached();
        for (Object value : VALUES) {
            assertThat(p.profile(state, value), is(value));
        }
        p.toString(); // test that it is not crashing
    }

}
