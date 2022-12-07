package ua.ithillel.tests;

import ua.ithillel.tests.annotations.AfterSuite;
import ua.ithillel.tests.annotations.BeforeSuite;
import ua.ithillel.tests.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class TestRunner {

    private static Method[] declaredMethods;

    private TestRunner() {
    }

    public static void start(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("THE CLASS PASSED AS AN ARGUMENT IS NULL");
        }
        declaredMethods = clazz.getDeclaredMethods();

        if (isTestClass()) {
            checkSingleInstanceMethods(clazz);

            Deque<Method> queue = new ArrayDeque<>(makeTestMap().values());

            Arrays.stream(declaredMethods)
                    .filter(method -> method.isAnnotationPresent(BeforeSuite.class))
                    .forEach(queue::addFirst);

            Arrays.stream(declaredMethods)
                    .filter(method -> method.isAnnotationPresent(AfterSuite.class))
                    .forEach(queue::addLast);

            startTests(queue);
        } else {
            System.out.println("THERE ARE NO TEST METHODS IN CLASS " + '{' + clazz.getName() + '}');
        }
    }

    private static void checkSingleInstanceMethods(Class<?> clazz) {
        List<Class<? extends Annotation>> annotations = new ArrayList<>();
        annotations.add(BeforeSuite.class);
        annotations.add(AfterSuite.class);

        annotations.forEach(aClass -> throwExceptionIfMoreThanOneAnnotation(aClass, clazz));
    }

    private static void throwExceptionIfMoreThanOneAnnotation(Class<? extends Annotation> aClass, Class<?> clazz) {
        List<Method> methods = new ArrayList<>();

        Arrays.stream(declaredMethods)
                .filter(method -> method.isAnnotationPresent(aClass))
                .forEach(methods::add);

        if (methods.size() != 1) {
            StringJoiner methodsNames = new StringJoiner(";", "[", "]");
            methods.forEach(method -> methodsNames.add(method.getName()));

            throw new IllegalArgumentException("MORE THAN ONE METHOD WITH ANNOTATION "
                    + '{' + aClass.getSimpleName() + '}'
                    + " IN CLASS " + '{' + clazz + '}'
                    + " ON METHODS: " + methodsNames);
        }
    }

    private static boolean isTestClass() {
        return Arrays.stream(declaredMethods).anyMatch(method -> method.isAnnotationPresent(Test.class));
    }

    private static Map<Integer, Method> makeTestMap() {
        return Arrays.stream(declaredMethods)
                .filter(method -> method.isAnnotationPresent(Test.class))
                .collect(Collectors.toMap(k -> k.getAnnotation(Test.class).value(), v -> v))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> newValue, TreeMap::new));
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
}