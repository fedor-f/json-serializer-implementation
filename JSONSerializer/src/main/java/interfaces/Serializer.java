package interfaces;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface Serializer {
    /**
     * Serializes an object and saves it to string.
     *
     * @param object an object to save
     * @return string representation of an object
     */
    String writeToString(Object object) throws IllegalAccessException;

    /**
     * Serializes an object and saves it to OutputStream.
     *
     * @param object       an object to save
     * @param outputStream an initial stream where an object is going to be saved
     * @throws IOException if there are problems with IO streams.
     */
    void write(Object object, OutputStream outputStream) throws IOException, IllegalAccessException;

    /**
     * Serializes an object and saves it to File.
     *
     * @param object an object to save
     * @param file   an initial file where an object is going to be saved
     * @throws IOException if there are problems with IO streams.
     */
    void write(Object object, File file) throws IOException, IllegalAccessException;
}