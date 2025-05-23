/*
 * Copyright (c) 2025, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.foreign;

import java.io.FileDescriptor;

import com.oracle.svm.core.NeverInline;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.AnnotateOriginal;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;
import com.oracle.svm.core.annotate.TargetClass;

import jdk.internal.access.foreign.MappedMemoryUtilsProxy;

@TargetClass(className = "java.nio.MappedMemoryUtils")
public final class Target_java_nio_MappedMemoryUtils {
    @Alias //
    @RecomputeFieldValue(isFinal = true, kind = Kind.None) //
    static MappedMemoryUtilsProxy PROXY;

    @Alias
    static native long mappingOffset(long address);

    @Alias
    static native long mappingLength(long mappingOffset, long length);

    @Alias
    static native long mappingAddress(long address, long mappingOffset);

    /*
     * Methods 'isLoaded', 'unload', and 'force' must not be inlined because they eventually do a
     * JNI call and inlining those will most certainly exhaust the inlining budget such that other
     * calls that need to be inlined (for correctness) cannot be inlined. Then, we will fail the
     * verification in SubstrateOptimizeSharedArenaAccessPhase#enumerateScopedAccesses.
     */
    @AnnotateOriginal
    @NeverInline("avoid inlining of JNI call")
    static native boolean isLoaded(long address, boolean isSync, long size);

    @AnnotateOriginal
    @NeverInline("avoid inlining of JNI call")
    static native void unload(long address, boolean isSync, long size);

    @AnnotateOriginal
    @NeverInline("avoid inlining of JNI call")
    static native void force(FileDescriptor fd, long address, boolean isSync, long index, long length);

    @Alias
    static native void load0(long address, long length);
}
