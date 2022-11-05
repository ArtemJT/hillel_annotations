package ua.ithillel.tests;

import ua.ithillel.tests.annotations.AfterSuite;
import ua.ithillel.tests.annotations.BeforeSuite;
import ua.ithillel.tests.annotations.Test;

public class TestingClass {

    @BeforeSuite
    public static void beforeSuite() {
        printMessage();
    }

    @Test(7)
    public static void test7() {
        printMessage();
    }

    @Test(9)
    public static void test9() {
        printMessage();
    }

    @Test(8)
    public static void test8() {
        printMessage();
    }

    @Test(6)
    public static void test6() {
        printMessage();
    }

    @Test(4)
    public static void test4() {
        printMessage();
    }

    @Test(5)
    public static void test5() {
        printMessage();
    }

    @Test(3)
    public static void test3() {
        printMessage();
    }

    @Test(1)
    public static void test1() {
        printMessage();
    }

    @Test(2)
    public static void test2() {
        printMessage();
    }

    @AfterSuite
    public static void afterSuite() {
        printMessage();
    }

    //@AfterSuite
    public static void afterSuite2() {
        printMessage();
    }

    private static void printMessage() {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        System.out.println(methodName + " COMPLETED");
    }
}
