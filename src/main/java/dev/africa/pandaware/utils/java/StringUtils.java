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

    public String replace(final String string, final String searchChars, String replaceChars) {
        if (string.isEmpty() || searchChars.isEmpty() || searchChars.equals(replaceChars))
            return string;

        if (replaceChars == null)
            replaceChars = "";

        final int stringLength = string.length();
        final int searchCharsLength = searchChars.length();
        final StringBuilder stringBuilder = new StringBuilder(string);

        for (int i = 0; i < stringLength; i++) {
            final int start = stringBuilder.indexOf(searchChars, i);

            if (start == -1) {
                if (i == 0)
                    return string;

                return stringBuilder.toString();
            }

            stringBuilder.replace(start, start + searchCharsLength, replaceChars);
        }

        return stringBuilder.toString();
    }

}
