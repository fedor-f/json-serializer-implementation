package implementation;

import exceptions.ExportedException;
import exceptions.PublicConstructorException;
import interfaces.Exported;
import interfaces.Serializer;
import interfaces.Restriction;

import java.io.*;
import java.lang.reflect.Modifier;

/**
 * Implementation of Serializer.
 */
public class DefaultSerializer implements Serializer, Restriction {
    @Override
    public String writeToString(Object object) throws IllegalAccessException {
        var jsonWriter = new JsonWriter(object);

        handleClassRestrictions(object);

        return jsonWriter
                .formatObject(object.getClass().getAnnotation(Exported.class)
                        .nullHandling().isIncluded());
    }

    @Override
    public void write(Object object, OutputStream outputStream) throws IOException, IllegalAccessException {
        JsonWriter jsonWriter = new JsonWriter(object);

        handleClassRestrictions(object);

        var bytes = jsonWriter
                .formatObject(object.getClass().getAnnotation(Exported.class)
                        .nullHandling().isIncluded()).getBytes();

        outputStream.write(bytes);

        outputStream.close();
    }

    @Override
    public void write(Object object, File file) throws IllegalAccessException, IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            JsonWriter jsonWriter = new JsonWriter(object);

            handleClassRestrictions(object);

            fileWriter.write(jsonWriter.formatObject(object.getClass().getAnnotation(Exported.class)
                    .nullHandling().isIncluded()));

        }
    }

    /**
     * Checks object's class for every needed restriction.
     *
     * @param object an object to check
     */
    private void handleClassRestrictions(Object object) {
        if (!object.getClass().isAnnotationPresent(Exported.class)) {
            throw new ExportedException(
                    String.format("The object %s you want to write is not @Exported",
                            object.getClass().getSimpleName()));
        }

        if (!hasConstructor(object) && !object.getClass().isRecord()) {
            throw new PublicConstructorException(
                    String.format("There is no public constructor with no parameters for class %s",
                            object.getClass().getSimpleName()));
        }
    }

    @Override
    public boolean hasConstructor(Object obj) {
        if (obj != null) {
            for (var constructor : obj.getClass().getConstructors()) {
                if (constructor.getParameterCount() == 0 && Modifier.isPublic(constructor.getModifiers())) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }
}