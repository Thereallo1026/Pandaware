package dev.africa.pandaware.impl.protection;

import dev.africa.pandaware.utils.client.HWIDUtils;
import dev.africa.pandaware.utils.java.crypt.EncryptionUtil;
import dev.africa.pandaware.utils.java.http.proprieties.header.HttpHeader;
import dev.africa.pandaware.utils.java.http.request.RequestBuilder;
import dev.africa.pandaware.utils.java.http.response.HttpResponse;

public class HWIDCheck {
    private final String secret;

    public HWIDCheck() {
        this.secret = System.getProperty("3a91f2f5-d4a5-4cf1-9288-64ab5801580b");

        //TODO: UNCOMMENT ON RELEASE
        //Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::check, 0L, 10L, TimeUnit.MINUTES);
    }

    void check() {
        try {
            String hwid = EncryptionUtil.encrypt(
                    HWIDUtils.getHWID(),
                    "literally_kill_yourself_lol"
            );

            HttpResponse resp = new RequestBuilder()
                    .url("http://157.245.91.112:42342/daa")
                    .header(new HttpHeader("d", hwid))
                    .body("literally_kill_yourself_lol")
                    .build()
                    .post();

            String selfDecrypt = EncryptionUtil.decrypt(hwid, "literally_kill_yourself_lol");
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