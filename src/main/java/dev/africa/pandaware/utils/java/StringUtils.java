package dev.africa.pandaware.utils.java;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public String[] splitFirstChar(String text) {
        char[] chars = text.toCharArray();
        StringBuilder rChars = new StringBuilder();

        for (char aChar : chars) {
            rChars.append(aChar);
        }

        String fChar = Character.toString(chars.length <= 0 ? '\n' : chars[0]);
        String remainingName = rChars.toString().replaceFirst(fChar, "");
        return new String[]{fChar, remainingName};
    }
}
