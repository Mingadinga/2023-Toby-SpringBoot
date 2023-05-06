package tobyspring.helloboot;

public interface HelloRepository {
    Hello findHello(String name);

    void increaseCount(String name);

    default int countOf(String name){
        Hello hello = findHello(name);
        return hello == null ? 0 : hello.getCount();
    }
}

// Comparator
// 인터페이스의 default 메소드가 어떻게 사용되는지 알고싶다면!