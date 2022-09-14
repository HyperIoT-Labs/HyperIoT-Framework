package it.acsoftware.hyperiot.base.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import org.reflections.Reflections;

import java.util.*;

/**
 * @Author Aristide Cittadino
 * This class provides all tests as a suite inside defined packages.
 * User can also set an execution order
 */
public class HyperIoTTestRunner {
    public static TestSuite createHyperIoTTestSuite(Comparator<Class<?>> testOrderComparator, String... packageStr) throws Throwable {
        List<Class<?>> testClasses = getAllTestClassesFromPackage(testOrderComparator, packageStr);
        TestSuite suite = new TestSuite();
        if (testClasses != null) {
            Iterator<Class<?>> it = testClasses.iterator();
            while (it.hasNext()) {
                JUnit4TestAdapter adapter = new JUnit4TestAdapter(it.next());
                suite.addTest(adapter);
            }
        }
        return suite;
    }

    public static TestSuite createHyperIoTTestSuite(String... packageStr) throws Throwable {
        return createHyperIoTTestSuite(Comparator.comparing(Class::getName), packageStr);
    }

    private static List<Class<?>> getAllTestClassesFromPackage(Comparator<Class<?>> testOrderComparator, String... packageStr) throws Throwable {
        Set<Class<?>> tests = null;
        for (int i = 0; i < packageStr.length; i++) {
            Set<Class<?>> packageTests = findHyperIoTTests(packageStr[i]);
            if (tests == null)
                tests = packageTests;
            else
                tests.addAll(packageTests);
        }
        return createOrderedTestList(tests, testOrderComparator);
    }

    private static Set<Class<?>> findHyperIoTTests(String packageStr) throws Throwable {
        Reflections reflections = new Reflections(packageStr);
        Set<Class<?>> testClasses = reflections.getTypesAnnotatedWith(RunWith.class);
        Set<Class<?>> suiteTests = new HashSet<>();
        Iterator<Class<?>> it = testClasses.iterator();
        while (it.hasNext()) {
            Class<?> classObj = it.next();
            RunWith annotation = classObj.getAnnotation(RunWith.class);
            if (annotation != null && !AllTests.class.isAssignableFrom(annotation.value())) {
                suiteTests.add(classObj);
            }
        }
        return suiteTests;
    }

    private static List<Class<?>> createOrderedTestList(Set<Class<?>> suiteTests, Comparator<Class<?>> testOrderComparator) {
        List<Class<?>> sortedTests = new ArrayList<>();
        suiteTests.stream().sorted(testOrderComparator).forEach(classObj -> sortedTests.add(classObj));
        return sortedTests;
    }
}
