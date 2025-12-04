/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.provider.mvstore;

/**
 * https://gitee.com/zhangjunfang/Chronicle-Map
 * <p>
 * https://stackoverflow.com/questions/62328831/using-chroniclemap-with-generics-java-lang-nosuchmethodexception-sun-nio-ch-fi
 * <p>
 * Getting intermittent exception with MVStore:
 * <p>
 * [ERROR] dev.ikm.tinkar.integration.coordinate.TestCoordinates.computeLatest  Time elapsed: 0.033 s  <<< ERROR!
 * java.lang.ClassCastException: class dev.ikm.tinkar.entity.ConceptRecord cannot be cast to class dev.ikm.tinkar.entity.SemanticEntity (dev.ikm.tinkar.entity.ConceptRecord and dev.ikm.tinkar.entity.SemanticEntity are in module dev.ikm.tinkar.entity@1.0-SNAPSHOT of loader 'app')
 * at dev.ikm.tinkar.provider.entity@1.0-SNAPSHOT/dev.ikm.tinkar.provider.entity.EntityProvider.lambda$textFast$0(EntityProvider.java:68)
 * at dev.ikm.tinkar.caffeine@3.0.2/com.github.benmanes.caffeine.cache.BoundedLocalCache.lambda$doComputeIfAbsent$13(BoundedLocalCache.java:2439)
 * at java.base/java.util.concurrent.ConcurrentHashMap.compute(ConcurrentHashMap.java:1955)
 * at dev.ikm.tinkar.caffeine@3.0.2/com.github.benmanes.caffeine.cache.BoundedLocalCache.doComputeIfAbsent(BoundedLocalCache.java:2437)
 * at dev.ikm.tinkar.caffeine@3.0.2/com.github.benmanes.caffeine.cache.BoundedLocalCache.computeIfAbsent(BoundedLocalCache.java:2420)
 * at dev.ikm.tinkar.caffeine@3.0.2/com.github.benmanes.caffeine.cache.LocalCache.computeIfAbsent(LocalCache.java:104)
 * at dev.ikm.tinkar.caffeine@3.0.2/com.github.benmanes.caffeine.cache.LocalManualCache.get(LocalManualCache.java:62)
 * at dev.ikm.tinkar.provider.entity@1.0-SNAPSHOT/dev.ikm.tinkar.provider.entity.EntityProvider.textFast(EntityProvider.java:63)
 * at dev.ikm.tinkar.common@1.0-SNAPSHOT/dev.ikm.tinkar.common.service.DefaultDescriptionForNidService.textOptional(DefaultDescriptionForNidService.java:27)
 * at dev.ikm.tinkar.common@1.0-SNAPSHOT/dev.ikm.tinkar.common.service.PrimitiveData.textOptional(PrimitiveData.java:127)
 * at dev.ikm.tinkar.common@1.0-SNAPSHOT/dev.ikm.tinkar.common.service.PrimitiveData.text(PrimitiveData.java:119)
 * at dev.ikm.tinkar.entity@1.0-SNAPSHOT/dev.ikm.tinkar.entity.SemanticRecord.entityToStringExtras(SemanticRecord.java:58)
 * at dev.ikm.tinkar.entity@1.0-SNAPSHOT/dev.ikm.tinkar.entity.Entity.entityToString(Entity.java:104)
 * at dev.ikm.tinkar.entity@1.0-SNAPSHOT/dev.ikm.tinkar.entity.SemanticRecord.toString(SemanticRecord.java:93)
 * at dev.ikm.tinkar.integration@1.0-SNAPSHOT/dev.ikm.tinkar.integration.coordinate.TestCoordinates.lambda$computeLatest$0(TestCoordinates.java:69)
 * at dev.ikm.tinkar.provider.entity@1.0-SNAPSHOT/dev.ikm.tinkar.provider.entity.EntityProvider.lambda$forEachSemanticForComponent$11b2ea8e$1(EntityProvider.java:221)
 * at dev.ikm.tinkar.eclipse.collections@11.0.0.M1/org.eclipse.collections.api.block.procedure.primitive.IntProcedure.accept(IntProcedure.java:30)
 * at dev.ikm.tinkar.provider.mvstore.MVStoreProvider.forEachSemanticNidForComponent(MVStoreProvider.java:250)
 * at dev.ikm.tinkar.provider.entity@1.0-SNAPSHOT/dev.ikm.tinkar.provider.entity.EntityProvider.forEachSemanticForComponent(EntityProvider.java:221)
 * at dev.ikm.tinkar.integration@1.0-SNAPSHOT/dev.ikm.tinkar.integration.coordinate.TestCoordinates.computeLatest(TestCoordinates.java:68)
 * at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 * at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
 * at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 * at java.base/java.lang.reflect.Method.invoke(Method.java:568)
 * at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:686)
 * at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
 * at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
 * at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:149)
 * at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:140)
 * at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:84)
 * at org.junit.jupiter.engine.execution.ExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(ExecutableInvoker.java:115)
 * at org.junit.jupiter.engine.execution.ExecutableInvoker.lambda$invoke$0(ExecutableInvoker.java:105)
 * at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
 * at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
 * at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
 * at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
 * at org.junit.jupiter.engine.execution.ExecutableInvoker.invoke(ExecutableInvoker.java:104)
 * at org.junit.jupiter.engine.execution.ExecutableInvoker.invoke(ExecutableInvoker.java:98)
 * at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$6(TestMethodTestDescriptor.java:212)
 * at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
 * at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:208)
 * at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:137)
 * at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:71)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$5(NodeTestTask.java:135)
 * at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$7(NodeTestTask.java:125)
 * at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:135)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:123)
 * at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:122)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:80)
 * at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
 * at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:38)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$5(NodeTestTask.java:139)
 * at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$7(NodeTestTask.java:125)
 * at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:135)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:123)
 * at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:122)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:80)
 * at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
 * at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:38)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$5(NodeTestTask.java:139)
 * at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$7(NodeTestTask.java:125)
 * at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:135)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:123)
 * at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:122)
 * at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:80)
 * at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:32)
 * at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
 * at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:51)
 * at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:248)
 * at org.junit.platform.launcher.core.DefaultLauncher.lambda$execute$5(DefaultLauncher.java:211)
 * at org.junit.platform.launcher.core.DefaultLauncher.withInterceptedStreams(DefaultLauncher.java:226)
 * at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:199)
 * at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:132)
 * at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:188)
 * at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:154)
 * at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:128)
 * at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:428)
 * at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162)
 * at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:562)
 * at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:548)
 * [INFO]
 */