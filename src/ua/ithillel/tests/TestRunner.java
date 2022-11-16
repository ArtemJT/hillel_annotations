package ua.ithillel.tests;

import ua.ithillel.tests.annotations.AfterSuite;
import ua.ithillel.tests.annotations.BeforeSuite;
import ua.ithillel.tests.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class TestRunner {

    public static void start(Class<?> clazz) {
        Deque<Method> queue = new ArrayDeque<>();

        if (clazz != null && isTestClass(clazz)) {
            checkSingleInstanceMethods(clazz);

            Method[] declaredMethods = clazz.getDeclaredMethods();

            queue.addAll(makeTestMap(declaredMethods).values());

            Arrays.stream(declaredMethods)
                    .filter(method -> method.isAnnotationPresent(BeforeSuite.class))
                    .forEach(queue::addFirst);

            Arrays.stream(declaredMethods)
                    .filter(method -> method.isAnnotationPresent(AfterSuite.class))
                    .forEach(queue::addLast);
        }

        startTests(queue);
    }

    private TestRunner() {
    }

    private static void startTests(Deque<Method> queue) {
        queue.forEach(method -> {
            try {
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Invoke exception in method " + '{' + method.getName() + '}');
            }
        });
    }

    private static void checkSingleInstanceMethods(Class<?> clazz) {
        int countBeforeAnn = 0;
        int countAfterAnn = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BeforeSuite.class)) countBeforeAnn++;
            if (method.isAnnotationPresent(AfterSuite.class)) countAfterAnn++;
        }
        if (countBeforeAnn != 1 && countAfterAnn != 1) {
            throw new IllegalArgumentException("MORE THAN ONE METHOD IN CLASS " + '{' + clazz.getSimpleName() + '}');
        }
    }

    private static boolean isTestClass(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).anyMatch(method -> method.isAnnotationPresent(Test.class));
    }

    private static Map<Integer, Method> makeTestMap(Method[] declaredMethods) {
        return Arrays.stream(declaredMethods)
                .filter(method -> method.isAnnotationPresent(Test.class))
                .collect(Collectors.toMap(k -> k.getAnnotation(Test.class).value(), v -> v))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> newValue, TreeMap::new));
    }
}