package dev.africa.pandaware.utils.java;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@UtilityClass
public class FileUtils implements MinecraftInstance {

    String randomLine;

    public File openFilePicker(boolean betterGui) throws Exception {
        if (Minecraft.getMinecraft().isFullScreen()) {
            Minecraft.getMinecraft().toggleFullscreen();
        }
        if (betterGui) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        JFileChooser fileChooser = new JFileChooser();
        JFrame frame = new JFrame();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        frame.setVisible(true);
        frame.toFront();
        frame.setVisible(false);

        frame.dispose();

        return (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null);
    }

    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                byte[] bytesIn = new byte[4096];
                int read;
                while ((read = zipIn.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, read);
                }
                bos.close();
            } else {
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    public String getStringFromResource(ResourceLocation resource) {
        return new BufferedReader(new InputStreamReader(getInputStreamFromResource(resource), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining(System.getProperty("line.separator")));
    }

    public InputStream getInputStreamFromResource(ResourceLocation resource) {
        try {
            return Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public void writeToFile(String content, File file) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFromFile(File file) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public String getRandomTitleLine() {
        String[] lines = {"Try death!",
                "Now on a computer near you!",
                "*Insert Dead Joke*",
                "KA is Overpowered",
                "The Return of Tenebrous!",
                "Hypixel: The Documentary.",
                "Brainless monkey moment.",
                "NCP , Verus , AAC",
                "The Grass is Greener on This Side",
                "Cheats: Not for Children Under the Age of 13.",
                "Bro...",
                "What the fuck?",
                "You have been banned from this server for 364d 23h 59m 59s!",
                "A staff member witnessed the use of cheats.",
                "The Water Fall Of Content!",
                "CRACKED BY xAnalPvP69x",
                "Novoline Ain't Got Nuthin on Me",
                "All's Well That Ends Well",
                "*Casual Reference to Your Mother*",
                "Yes, but no",
                "Freak.",
                "So when's the last time you showered huh?",
                "Ok.",
                "Now with more things to kill you!",
                "Now compatible with the IBM Model M.",
                "I Pity the Tools...",
                "The ban hammer has SPOKEN!",
                "So remember when I asked?",
                "Subscribe to CallumUncensored",
                "Pandas... yeah, cool animals right?",
                "Coming soon to a computer near you",
                "Dividing by zero!",
                "Now with EPIC BLUR AND GLOW!",
                "Press alt-f4",
                "I Pity the skidders.",
                "You ok bro?",
                "NOT THE BEES!!!",
                "Legend of Novoline",
                "Also try Terraria!",
                "Shut Up and Play!",
                "2: Electric Boogaloo",
                "Now with more shitcode",
                "Not skidded from Tenacity",
                "Sentinel caught you cheating (Anticheat)",
                "Now with more else-if chains!",
                "Cracked by coinful & kant",
                "Roy Hwang edition",
                "Ryan Davies edition",
                "مثل دسار ديك أسود",
                "ching chong",
                "アストルフォ抱き枕を送ってください",
                "Developed at 2710 English Ivy Ct. Longwood, Florida",
                "no autoblock?",
                "Do you like my ascii penis?",
                "RIP your chances at losing your virginity"};
        return lines[RandomUtils.nextInt(0, lines.length)];
    }


}
