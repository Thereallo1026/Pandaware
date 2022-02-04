package dev.africa.pandaware.utils.java;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
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
}
