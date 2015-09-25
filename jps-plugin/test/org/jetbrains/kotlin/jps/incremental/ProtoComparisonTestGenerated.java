/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.jps.incremental;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.JetTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@RunWith(JUnit3RunnerWithInners.class)
public class ProtoComparisonTestGenerated extends AbstractProtoComparisonTest {
    @TestMetadata("jps-plugin/testData/comparison/classSignatureChange")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class ClassSignatureChange extends AbstractProtoComparisonTest {
        public void testAllFilesPresentInClassSignatureChange() throws Exception {
            JetTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("jps-plugin/testData/comparison/classSignatureChange"), Pattern.compile("^([^\\.]+)$"), true);
        }

        @TestMetadata("classFlagsChanged")
        public void testClassFlagsChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classSignatureChange/classFlagsChanged/");
            doTest(fileName);
        }

        @TestMetadata("classToPackageFacade")
        public void testClassToPackageFacade() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classSignatureChange/classToPackageFacade/");
            doTest(fileName);
        }

        @TestMetadata("classTypeParameterListChanged")
        public void testClassTypeParameterListChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classSignatureChange/classTypeParameterListChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithClassAnnotationListChanged")
        public void testClassWithClassAnnotationListChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classSignatureChange/classWithClassAnnotationListChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithSuperTypeListChanged")
        public void testClassWithSuperTypeListChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classSignatureChange/classWithSuperTypeListChanged/");
            doTest(fileName);
        }

        @TestMetadata("packageFacadeToClass")
        public void testPackageFacadeToClass() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classSignatureChange/packageFacadeToClass/");
            doTest(fileName);
        }

    }

    @TestMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class ClassPrivateOnlyChange extends AbstractProtoComparisonTest {
        public void testAllFilesPresentInClassPrivateOnlyChange() throws Exception {
            JetTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("jps-plugin/testData/comparison/classPrivateOnlyChange"), Pattern.compile("^([^\\.]+)$"), true);
        }

        @TestMetadata("classWithPrivateFunChanged")
        public void testClassWithPrivateFunChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange/classWithPrivateFunChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithPrivatePrimaryConstructorChanged")
        public void testClassWithPrivatePrimaryConstructorChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange/classWithPrivatePrimaryConstructorChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithPrivateSecondaryConstructorChanged")
        public void testClassWithPrivateSecondaryConstructorChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange/classWithPrivateSecondaryConstructorChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithPrivateValChanged")
        public void testClassWithPrivateValChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange/classWithPrivateValChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithPrivateVarChanged")
        public void testClassWithPrivateVarChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange/classWithPrivateVarChanged/");
            doTest(fileName);
        }

        @TestMetadata("packageFacadeDifference")
        public void testPackageFacadeDifference() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange/packageFacadeDifference/");
            doTest(fileName);
        }

        @TestMetadata("packageFacadeMultifileClassDifference")
        public void testPackageFacadeMultifileClassDifference() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classPrivateOnlyChange/packageFacadeMultifileClassDifference/");
            doTest(fileName);
        }
    }

    @TestMetadata("jps-plugin/testData/comparison/classMembersOnlyChanged")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class ClassMembersOnlyChanged extends AbstractProtoComparisonTest {
        public void testAllFilesPresentInClassMembersOnlyChanged() throws Exception {
            JetTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("jps-plugin/testData/comparison/classMembersOnlyChanged"), Pattern.compile("^([^\\.]+)$"), true);
        }

        @TestMetadata("classWithCompanionObjectChanged")
        public void testClassWithCompanionObjectChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classMembersOnlyChanged/classWithCompanionObjectChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithConstructorChanged")
        public void testClassWithConstructorChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classMembersOnlyChanged/classWithConstructorChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithFunAndValChanged")
        public void testClassWithFunAndValChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classMembersOnlyChanged/classWithFunAndValChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWithNestedClassesChanged")
        public void testClassWithNestedClassesChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classMembersOnlyChanged/classWithNestedClassesChanged/");
            doTest(fileName);
        }

        @TestMetadata("classWitnEnumChanged")
        public void testClassWitnEnumChanged() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classMembersOnlyChanged/classWitnEnumChanged/");
            doTest(fileName);
        }

        @TestMetadata("packageFacadeDifference")
        public void testPackageFacadeDifference() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/classMembersOnlyChanged/packageFacadeDifference/");
            doTest(fileName);
        }

    }

    @TestMetadata("jps-plugin/testData/comparison/unchanged")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Unchanged extends AbstractProtoComparisonTest {
        public void testAllFilesPresentInUnchanged() throws Exception {
            JetTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("jps-plugin/testData/comparison/unchanged"), Pattern.compile("^([^\\.]+)$"), true);
        }

        @TestMetadata("unchangedClass")
        public void testUnchangedClass() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/unchanged/unchangedClass/");
            doTest(fileName);
        }

        @TestMetadata("unchangedPackageFacade")
        public void testUnchangedPackageFacade() throws Exception {
            String fileName = JetTestUtils.navigationMetadata("jps-plugin/testData/comparison/unchanged/unchangedPackageFacade/");
            doTest(fileName);
        }

    }
}
