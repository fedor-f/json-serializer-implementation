package implementation;

import exceptions.ExportedException;
import exceptions.PublicConstructorException;
import interfaces.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSerializerTest {

    @Test
    void writeToStringFirst() throws IllegalAccessException {
        String expected = "{\"bool\":false}";
        Serializer serializer = new DefaultSerializer();
        TestClassFirst testClass = new TestClassFirst();

        assertEquals(expected, serializer.writeToString(testClass));
    }

    @Test
    void writeToStringSecond() {
        Serializer serializer = new DefaultSerializer();
        TestClassSecond testClass = new TestClassSecond();

        assertThrows(ExportedException.class, () -> serializer.writeToString(testClass));
    }

    @Test
    void writeToStringThird() throws IllegalAccessException {
        String expected = "{\"string\":null,\"boolean value\":false}";
        Serializer serializer = new DefaultSerializer();
        TestClassThird testClass = new TestClassThird();

        assertEquals(expected, serializer.writeToString(testClass));
    }

    @Test
    void writeToStringFourth() {
        Serializer serializer = new DefaultSerializer();
        TestClassFourth testClass = new TestClassFourth(0);

        assertThrows(PublicConstructorException.class, () -> serializer.writeToString(testClass));
    }

    @Test
    void writeToStringFifth() throws IllegalAccessException {
        String expected = "{\"ldt\":\"01/01/+1000000000 12:00:00\",\"ld\":\"31/12/+999999999\",\"lt\":\"11:59:59\"}";
        Serializer serializer = new DefaultSerializer();
        TestClassFifth testClass = new TestClassFifth();

        assertEquals(expected, serializer.writeToString(testClass));
    }

    @Test
    void writeToStringSixth() throws IllegalAccessException {
        String expected = "{\"testField\":{\"bool\":false}}";

        TestClassSixth test = new TestClassSixth();
        Serializer serializer = new DefaultSerializer();

        assertEquals(expected, serializer.writeToString(test));
    }

    @Test
    void writeToStringSeventh() throws IllegalAccessException {
        String expected = "{\"list\":[\"TestClassFirst\":{\"bool\":false},\"TestClassFirst\":{\"bool\":false}]}";
        TestClassSeventh testClass = new TestClassSeventh();
        Serializer serializer = new DefaultSerializer();

        assertEquals(expected, serializer.writeToString(testClass));
    }

    @Test
    void writeToStringEights() throws IllegalAccessException {
        String expected = "{\"list\":null}";
        TestClassEights test = new TestClassEights();
        Serializer serializer = new DefaultSerializer();

        assertEquals(expected, serializer.writeToString(test));
    }

    @Test
    void write() throws IOException, IllegalAccessException {
        File file = new File("src/test/resources/test.txt");

        TestClassFirst test = new TestClassFirst();
        Serializer serializer = new DefaultSerializer();

        assertDoesNotThrow(() -> serializer.write(test, file));
    }

    @Test
    void testWrite() throws FileNotFoundException {
        OutputStream stream = new FileOutputStream("src/test/resources/testStream.txt");

        TestClassEights test = new TestClassEights();

        Serializer serializer = new DefaultSerializer();

        assertDoesNotThrow(() -> serializer.write(test, stream));
    }
}

@Exported
class TestClassFirst {
    static Integer number = 5;
    private String string;
    private boolean bool = false;

    public TestClassFirst() {
    }
}

class TestClassSecond {
    static Integer number = 5;
    private String string;
    private boolean bool = false;

    public TestClassSecond() {
    }
}

@Exported(nullHandling = NullHandling.INCLUDE)
class TestClassThird {
    private String string;

    @Ignored
    public Integer ignoreMe;

    @PropertyName("boolean value")
    private boolean bool = false;

    public TestClassThird() {
    }
}

@Exported(nullHandling = NullHandling.INCLUDE)
class TestClassFourth {
    @PropertyName("boolean value")
    private boolean bool = false;

    public TestClassFourth(int number) {
    }
}

@Exported(nullHandling = NullHandling.INCLUDE)
class TestClassFifth {
    @DateFormat(pattern = "dd/MM/yyyy hh:mm:ss")
    LocalDateTime ldt = LocalDateTime.MIN;

    @DateFormat(pattern = "dd/MM/yyyy")
    LocalDate ld = LocalDate.MAX;

    @DateFormat(pattern = "hh:mm:ss")
    LocalTime lt = LocalTime.MAX;

    public TestClassFifth() {
    }
}

@Exported
class TestClassSixth {
    public TestClassFirst testField = new TestClassFirst();

    public TestClassSixth() {

    }
}

@Exported(nullHandling = NullHandling.EXCLUDE)
class TestClassSeventh {
    public List<TestClassFirst> list = new ArrayList<>();

    public TestClassSeventh() {
        list.add(new TestClassFirst());
        list.add(new TestClassFirst());
    }
}

@Exported(nullHandling = NullHandling.INCLUDE)
class TestClassEights {
    public List<TestClassFirst> list = null;

    public TestClassEights() {
    }
}