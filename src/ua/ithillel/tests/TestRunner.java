package ua.ithillel.tests;

import ua.ithillel.tests.annotations.AfterSuite;
import ua.ithillel.tests.annotations.BeforeSuite;
import ua.ithillel.tests.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestRunner {

    public static void start(Class<?> clazz) {
        Deque<Method> queue = new ArrayDeque<>();

        if (clazz != null) {
            if (isTestClass(clazz)) {
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
        }

        startTests(queue);
    }

    private static void startTests(Deque<Method> queue) {
        queue.forEach(method -> {
            try {
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Invoke exception in method " + '{' + method.getName() + '}');
            }
        });
    }

    private static void checkSingleInstanceMethods(Class<?> clazz) {
        AtomicInteger countBeforeAnn = new AtomicInteger();
        AtomicInteger countAfterAnn = new AtomicInteger();
        Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> {
            if (method.isAnnotationPresent(BeforeSuite.class)) countBeforeAnn.getAndIncrement();
            if (method.isAnnotationPresent(AfterSuite.class)) countAfterAnn.getAndIncrement();
        });
        if (countBeforeAnn.get() != 1 && countAfterAnn.get() != 1) {
            throw new RuntimeException("MORE THAN ONE METHOD IN CLASS " + '{' + clazz.getSimpleName() + '}');
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