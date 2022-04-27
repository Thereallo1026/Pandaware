package dev.africa.packetapi.util;

import java.io.*;
import java.util.Base64;

public class SerialUtil {
    public static String serialize(Serializable serializable) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(serializable);
        objectOutputStream.close();

        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    public static <T> T deserialize(String s, Class<T> type) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(s);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));

        Object object = objectInputStream.readObject();
        objectInputStream.close();

        return (T) object;
    }
}
