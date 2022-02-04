package dev.africa.pandaware.impl.font;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.interfaces.Initializable;
import dev.africa.pandaware.impl.font.renderer.TTFFontRenderer;
import dev.africa.pandaware.utils.client.Printer;
import lombok.Getter;

import java.awt.*;
import java.util.Objects;

@Getter
public class Fonts implements Initializable {
    @Getter
    private static final Fonts instance = new Fonts();

    private TTFFontRenderer arialBdSmall, arialBdNormal, arialBdMedium, arialBdBig, arialBdVeryBig;
    private TTFFontRenderer comfortaSmall, comfortaNormal, comfortaMedium, comfortaBig, comfortaVeryBig;
    private TTFFontRenderer iconsSmall, iconsNormal, iconsMedium, iconsBig, iconsVeryBig;
    private TTFFontRenderer productSansSmall, productSansNormal, productSansMedium, productSansBig, productSansVeryBig;

    @Override
    public void init() {
        try {
            this.arialBdSmall = this.createFont("arialbd.ttf", 16);
            this.arialBdNormal = this.createFont("arialbd.ttf", 19);
            this.arialBdMedium = this.createFont("arialbd.ttf", 20);
            this.arialBdBig = this.createFont("arialbd.ttf", 24);
            this.arialBdVeryBig = this.createFont("arialbd.ttf", 75);

            this.comfortaSmall = this.createFont("Comfortaa-Regular.ttf", 16);
            this.comfortaNormal = this.createFont("Comfortaa-Regular.ttf", 19);
            this.comfortaMedium = this.createFont("Comfortaa-Regular.ttf", 20);
            this.comfortaBig = this.createFont("Comfortaa-Regular.ttf", 24);
            this.comfortaVeryBig = this.createFont("Comfortaa-Regular.ttf", 30);

            this.iconsSmall = this.createFont("Icons.ttf", 16);
            this.iconsNormal = this.createFont("Icons.ttf", 19);
            this.iconsMedium = this.createFont("Icons.ttf", 20);
            this.iconsBig = this.createFont("Icons.ttf", 24);
            this.iconsVeryBig = this.createFont("Icons.ttf", 34);

            this.productSansSmall = this.createFont("productsans.ttf", 16);
            this.productSansNormal = this.createFont("productsans.ttf", 19);
            this.productSansMedium = this.createFont("productsans.ttf", 20);
            this.productSansBig = this.createFont("productsans.ttf", 24);
            this.productSansVeryBig = this.createFont("productsans.ttf", 30);
        } catch (Exception e) {
            Printer.consoleError("Failed to load fonts, StackTrace: " + e.getMessage());
            System.exit(2);
        }
    }

    public TTFFontRenderer createFont(String fontName, int size) throws Exception {
        String fontPath = "assets/minecraft/" + Client.getInstance().getManifest()
                .getClientName().toLowerCase() + "/fonts/";

        return new TTFFontRenderer(Font.createFont(Font.PLAIN,
                Objects.requireNonNull(ClassLoader.getSystemClassLoader()
                        .getResourceAsStream(fontPath + fontName))).deriveFont(Font.PLAIN, size));
    }
}
