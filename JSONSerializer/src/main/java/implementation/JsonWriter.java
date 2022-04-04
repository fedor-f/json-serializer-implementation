package implementation;

import exceptions.ExportedException;
import exceptions.PublicConstructorException;
import interfaces.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles all operations when formatting an object to string in JSON format.
 */
public class JsonWriter implements Restriction {
    /**
     * An object that is going to be formatted.
     */
    private final Object objectToFormat;

    public JsonWriter(Object obj) {
        objectToFormat = obj;
    }

    /**
     * Formats an object to JSON format string.
     *
     * @param nullHandling flag to check if class uses null values for serialization
     * @return JSON representation of an object.
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    public String formatObject(boolean nullHandling) throws IllegalAccessException {
        var fields = objectToFormat.getClass().getDeclaredFields();

        StringBuilder stringBuilder = new StringBuilder("{");

        formatDifferentTypes(fields, stringBuilder, objectToFormat, nullHandling);

        stringBuilder.append("}");

        deleteRedundantCommas(stringBuilder);

        return stringBuilder.toString();
    }

    /**
     * Deleting redundant commas in a string in JSON format.
     *
     * @param stringBuilder StringBuilder object to handle operations with a string
     */
    private void deleteRedundantCommas(StringBuilder stringBuilder) {
        // Deletes commas before closing brackets.
        for (int i = 0; i < stringBuilder.toString().length() - 1; i++) {
            if (stringBuilder.toString().charAt(i) == ',' &&
                    (stringBuilder.toString().charAt(i + 1) == '}' || stringBuilder.toString().charAt(i + 1) == ']')) {
                stringBuilder.deleteCharAt(i);
            }
        }
    }

    /**
     * Formats every type to string in JSON format.
     *
     * @param fields        an array of fields
     * @param stringBuilder StringBuilder object to handle string operations
     * @param obj           serializing object
     * @param nullHandling  flag to check if class uses null values for serialization
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private void formatDifferentTypes(Field[] fields, StringBuilder stringBuilder, Object obj, boolean nullHandling)
            throws IllegalAccessException {
        for (var i = 0; i < fields.length; i++) {

            fields[i].setAccessible(true);

            if (handleFieldRestrictions(fields, i)) {
                continue;
            }

            String propertyName = processPropertyNameAnnotation(fields, i);

            Class<?> genericType = getGenericType(fields, i);

            if (isCollection(fields[i]) && !isCorrectGenericType(genericType)) {
                if (nullHandling && fields[i].get(obj) == null) {
                    stringBuilder.append(String.format("\"%s\":null,", propertyName));
                } else if (fields[i].get(obj) != null) {
                    stringBuilder.append(String.format("\"%s\":[", propertyName));

                    processCollection(stringBuilder, obj, fields[i], nullHandling);

                    stringBuilder.append("],");
                }
            } else if (isWrapperOrPrimitive(fields[i]) || isCollection(fields[i])) {
                formatStringsOrSimpleTypes(fields, stringBuilder, obj, i, nullHandling, propertyName);
            } else if (!isWrapperOrPrimitive(fields[i])) {
                processCustomTypes(fields, stringBuilder, obj, nullHandling, i, propertyName);
            }
        }
    }

    /**
     * Formatting custom types to string in JSON format recursively.
     *
     * @param fields        an array of fields
     * @param stringBuilder StringBuilder object to handle string operations
     * @param object        serializing object
     * @param nullHandling  flag to check if class uses null values for serialization
     * @param index         index of a field in an array of fields
     * @param propertyName  name of a property
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private void processCustomTypes(Field[] fields, StringBuilder stringBuilder, Object object, boolean nullHandling,
                                    int index, String propertyName) throws IllegalAccessException {
        checkRestrictionsForFieldType(fields, object, index);

        if (!nullHandling && fields[index].get(object) == null) {
            return;
        }

        stringBuilder.append(String.format("\"%s\":", propertyName));

        if (fields[index].get(object) != null) {
            stringBuilder.append("{");

            formatDifferentTypes(fields[index].getType().getDeclaredFields(),
                    stringBuilder, fields[index].get(object),
                    fields[index].get(object).getClass().getAnnotation(Exported.class)
                            .nullHandling().isIncluded());

            stringBuilder.append("},");
        } else {
            stringBuilder.append("null,");
        }
    }

    /**
     * Gets generic type if a field's type is collection.
     *
     * @param fields an array of fields
     * @param index  index of a field in an array of fields
     * @return class instance if generic type
     */
    private Class<?> getGenericType(Field[] fields, int index) {
        ParameterizedType type;
        Class<?> genericType = null;
        if (isCollection(fields[index])) {
            type = (ParameterizedType) fields[index].getGenericType();
            genericType = (Class<?>) type.getActualTypeArguments()[0];
        }
        return genericType;
    }

    /**
     * Checks field restrictions i.e. if a field is synthetic or static,
     * or if a field marked as @Ignored.
     *
     * @param fields an array of fields
     * @param index  an index of a field in a field array
     * @return true if a field is synthetic or static, of marked as @Ignored, otherwise, false
     */
    private boolean handleFieldRestrictions(Field[] fields, int index) {
        if (fields[index].isSynthetic() || Modifier.isStatic(fields[index].getModifiers())) {
            return true;
        }

        return fields[index].isAnnotationPresent(Ignored.class);
    }

    /**
     * Sets new property's name if it is marked as @PropertyName.
     *
     * @param fields an array of fields.
     * @param index  an index of a field in an array of fields
     * @return string representation of a new property's name
     */
    private String processPropertyNameAnnotation(Field[] fields, int index) {
        var propertyName = fields[index].getName();
        if (fields[index].isAnnotationPresent(PropertyName.class)) {
            propertyName = fields[index].getAnnotation(PropertyName.class).value();
        }
        return propertyName;
    }

    /**
     * Gets all properties of a collection of custom types and tries to format to JSON.
     *
     * @param stringBuilder StringBuilder object to process operations with strings
     * @param object        serializing object
     * @param fields        an array of fields
     * @param nullHandling  flag to check if class uses null values for serialization
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private void processCollection(StringBuilder stringBuilder, Object object, Field fields, boolean nullHandling)
            throws IllegalAccessException {
        List<?> list = castCollection(object, fields);

        for (var listObject : list) {
            if (!nullHandling && listObject == null) {
                continue;
            }

            if (listObject != null) {
                checkRestrictionsOfObjectOfCollection(listObject);

                stringBuilder.append(String.format("\"%s\":{", listObject.getClass().getSimpleName()));

                handleListRecursively(listObject, stringBuilder, nullHandling);

                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append("},");


            } else {
                stringBuilder.append("null,");
            }
        }
    }

    /**
     * Casts collection to a List.
     *
     * @param object serializing object
     * @param field  field of a collection type
     * @return new list of cast collection
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private List<?> castCollection(Object object, Field field) throws IllegalAccessException {
        List<?> list = new ArrayList<>();
        if (field.getType() == List.class) {
            list = new ArrayList<>((List<?>) field.get(object));
        } else if (field.getType() == Set.class) {
            list = new ArrayList<>((Set<?>) field.get(object));
        }
        return list;
    }

    /**
     * Checks restrictions for objects of a collection of custom types.
     *
     * @param object object from collection
     */
    private void checkRestrictionsOfObjectOfCollection(Object object) {
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

    /**
     * Formats objects in list recursively.
     *
     * @param object        object in list
     * @param stringBuilder StringBuilder object to process strings
     * @param nullHandling  flag to check if class uses null values for serialization
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private void handleListRecursively(Object object, StringBuilder stringBuilder, boolean nullHandling)
            throws IllegalAccessException {
        var fields = object.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);

            if (handleFieldRestrictions(fields, i)) {
                continue;
            }

            if (fields[i].isAnnotationPresent(Ignored.class)) {
                continue;
            }

            var propertyName = fields[i].getName();
            if (fields[i].isAnnotationPresent(PropertyName.class)) {
                propertyName = fields[i].getAnnotation(PropertyName.class).value();
            }

            if (fields[i].get(object) == null && !nullHandling) {
                continue;
            }

            if (fields[i].get(object) != null) {
                if (isWrapperOrPrimitive(fields[i])) {
                    if (fields[i].getType() == String.class && fields[i].get(object) != null) {
                        stringBuilder.append(String.format("\"%s\":\"%s\", ", propertyName, fields[i].get(object)));
                    } else {
                        if (fields[i].isAnnotationPresent(DateFormat.class)) {
                            var value = handleDateFormatAnnotation(fields, object, i);

                            stringBuilder.append(String.format("\"%s\":%s, ", propertyName, value));
                        } else {
                            stringBuilder.append(String.format("\"%s\":%s, ", propertyName, fields[i].get(object)));
                        }
                    }
                } else {
                    checkRestrictionsForFieldType(fields, object, i);

                    stringBuilder.append(String.format("\"%s\":{", propertyName));

                    handleListRecursively(fields[i].get(object), stringBuilder, nullHandling);

                    stringBuilder.append("},");
                }
            } else {
                try {
                    var field = object.getClass().getDeclaredField(fields[i].getName());
                    Class<?> clazz = field.getType();

                    if (!clazz.isAnnotationPresent(Exported.class) && !isWrapperOrPrimitive(field)) {
                        throw new ExportedException(
                                String.format("The object %s you want to write is not @Exported",
                                        clazz.getSimpleName()));
                    }

                    stringBuilder.append(String.format("\"%s\":null, ", propertyName));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checks if class is marked as @Exported,
     * and checks if it has constructor with no parameters or if it is a record.
     *
     * @param fields an array of fields
     * @param object serializing object
     * @param index  index of a field in an array of fields
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private void checkRestrictionsForFieldType(Field[] fields, Object object, int index) throws IllegalAccessException {
        if (!fields[index].getType().isAnnotationPresent(Exported.class)) {
            throw new ExportedException(
                    String.format("The object %s you want to write is not @Exported",
                            fields[index].getType().getSimpleName()));
        }

        if (!hasConstructor(fields[index].get(object)) && !fields[index].get(object).getClass().isRecord()) {
            throw new PublicConstructorException(
                    String.format("There is no public constructor with no parameters for class %s",
                            fields[index].get(object).getClass().getSimpleName()));
        }
    }

    /**
     * Formats fields of not custom types in JSON format.
     *
     * @param fields        an array of fields
     * @param stringBuilder StringBuilder object to process operations with strings
     * @param object        serializing object
     * @param index         index of a field in an array of fields
     * @param nullHandling  flag to check if class uses null values for serialization
     * @param propertyName  name of a property
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private void formatStringsOrSimpleTypes(Field[] fields, StringBuilder stringBuilder,
                                            Object object, int index, boolean nullHandling,
                                            String propertyName) throws IllegalAccessException {
        if (fields[index].getType() == String.class && fields[index].get(object) != null) {
            stringBuilder.append(String.format("\"%s\":\"%s\",", propertyName, fields[index].get(object)));
        } else if (nullHandling || fields[index].get(object) != null) {
            if (fields[index].isAnnotationPresent(DateFormat.class)) {
                var value = handleDateFormatAnnotation(fields, object, index);

                stringBuilder.append(String.format("\"%s\":\"%s\",", propertyName, value));
            } else {
                stringBuilder.append(String.format("\"%s\":%s,", propertyName, fields[index].get(object)));
            }
        }

    }

    /**
     * Handles fields that marked as @DateFormat.
     *
     * @param fields an array of fields
     * @param object serializing object
     * @param index  an index of a field in an array of fields
     * @return string representation of a new format of fields of types LocalDate, LocalTime, LocalDateTime
     * @throws IllegalAccessException if an illegal access attempt has occurred
     */
    private String handleDateFormatAnnotation(Field[] fields, Object object, int index) throws IllegalAccessException {
        var format =
                DateTimeFormatter.ofPattern(fields[index].getAnnotation(DateFormat.class).pattern());

        String value = " ";

        if (fields[index].get(object) instanceof LocalDate) {
            value = ((LocalDate) fields[index].get(object)).format(format);
        } else if (fields[index].get(object) instanceof LocalDateTime) {
            value = ((LocalDateTime) fields[index].get(object)).format(format);
        } else if (fields[index].get(object) instanceof LocalTime) {
            value = ((LocalTime) fields[index].get(object)).format(format);
        }

        return value;
    }

    /**
     * Checks if class is a simple type, not custom.
     *
     * @param type checking class
     * @return true if a class is wrapper, LocalDate, LocalTime, LocalDateTime, otherwise, false
     */
    private boolean isCorrectGenericType(Class<?> type) {
        return type.getSimpleName().equals("Integer") ||
                type.getSimpleName().equals("Double") ||
                type.getSimpleName().equals("Long") ||
                type.getSimpleName().equals("Float") ||
                type.getSimpleName().equals("Short") ||
                type.getSimpleName().equals("Character") ||
                type.getSimpleName().equals("String") ||
                type.getSimpleName().equals("Byte") ||
                type.getSimpleName().equals("Boolean") ||
                type.getSimpleName().equals("LocalDate") ||
                type.getSimpleName().equals("LocalTime") ||
                type.getSimpleName().equals("LocalDateTime");
    }

    /**
     * Checks a field's type is primitive, wrapper, LocalDate, LocalDateTime, LocalTime, Enum.
     *
     * @param field checking field
     * @return true if a field's type is one of
     * primitive, wrapper, LocalDate, LocalDateTime, LocalTime, Enum, otherwise, false
     */
    private boolean isWrapperOrPrimitive(Field field) {
        return field.getType() == Double.class ||
                field.getType() == Float.class ||
                field.getType() == Long.class ||
                field.getType() == Integer.class ||
                field.getType() == Short.class ||
                field.getType() == Character.class ||
                field.getType() == Byte.class ||
                field.getType() == Boolean.class ||
                field.getType().isPrimitive() ||
                field.getType() == String.class ||
                field.getType().isEnum() ||
                field.getType() == LocalDate.class ||
                field.getType() == LocalTime.class ||
                field.getType() == LocalDateTime.class;
    }

    /**
     * Checks if a field's type is a collection of List or Set.
     *
     * @param field some field
     * @return true if a field is collection, otherwise, false
     */
    private boolean isCollection(Field field) {
        return field.getType() == List.class ||
                field.getType() == Set.class;
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
