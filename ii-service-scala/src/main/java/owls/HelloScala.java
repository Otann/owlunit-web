package owls;

public class HelloScala {
    public static void helloScala() {
        System.out.println("Hello from Java");

        // call scala class
        System.out.println("Scala says: " + App.helloJava());
        System.out.println("Scala says: " + new PersonTest().getName() + new PersonTest().isBadass());
    }
}
