package dev.africa.pandaware.impl.file;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.file.FileObject;
import dev.africa.pandaware.utils.java.FileUtils;

import java.io.File;
import java.io.FileReader;

public class SettingsFile extends FileObject {
    public SettingsFile(File rootFolder, String fileName, Gson gson) {
        super(rootFolder, fileName, gson);
    }

    @Override
    public void save() throws Exception {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("firstLaunch", false);

        FileUtils.writeToFile(this.getGson().toJson(jsonObject), this.getFile());
    }

    @Override
    public void load() throws Exception {
        JsonObject jsonParser = JsonParser.parseReader(new FileReader(this.getFile())).getAsJsonObject();

        Client.getInstance().setFirstLaunch(jsonParser.get("firstLaunch").getAsBoolean());
    }
}
