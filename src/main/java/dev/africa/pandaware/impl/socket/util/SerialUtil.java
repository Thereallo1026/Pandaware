package dev.africa.pandaware.impl.socket.util;

import java.io.*;
import java.util.Base64;

public class SerialUtil {

    public static String serializeObject(Serializable serializable) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(serializable);
        objectOutputStream.close();

        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    public static Object serializeFromString(String s) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(s);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));

        Object object = objectInputStream.readObject();
        objectInputStream.close();

        return object;
    }
}
