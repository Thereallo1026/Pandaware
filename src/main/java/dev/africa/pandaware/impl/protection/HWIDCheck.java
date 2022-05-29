package dev.africa.pandaware.impl.protection;

import dev.africa.pandaware.utils.client.HWIDUtils;
import dev.africa.pandaware.utils.java.crypt.EncryptionUtil;
import dev.africa.pandaware.utils.java.http.proprieties.header.HttpHeader;
import dev.africa.pandaware.utils.java.http.request.RequestBuilder;
import dev.africa.pandaware.utils.java.http.response.HttpResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HWIDCheck {
    private final String secret;

    public HWIDCheck() {
        this.secret = System.getProperty("98aef67c-7cfe-4cb2-afc4-17fe02efdf37");

        //DONT UNCOMMENT UNLESS RELEASE VERSION
        //Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::check, 0L, 10L, TimeUnit.SECONDS);
    }

    void check() {
        try {
            String hwid = EncryptionUtil.encrypt(
                    HWIDUtils.getHWID(),
                    this.secret
            );

            String headerKey = EncryptionUtil.encrypt(
                    this.secret,
                    "sorrymen{hava_way_way_way}"
            );

            HttpResponse resp = new RequestBuilder()
                    .url("http://157.245.91.112:42342/daa")
                            .header(new HttpHeader("d", hwid))
                            .body(headerKey)
                            .build()
                            .post();

            String selfDecrypt = EncryptionUtil.decrypt(hwid, this.secret);
            String decrypt = resp.getBody();

            if (selfDecrypt.equals(decrypt)) {
                return;
            }

            if (System.getProperty("fcb4a890-3d2f-4c50-895a-845b4dde1a12") == null) {
                System.exit(2);
            }

            System.exit(4);
        } catch (Exception e) {
            System.exit(5);
        }
    }
}