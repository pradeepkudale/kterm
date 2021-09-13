package com.pradale.kterm.utils;

import org.apache.sshd.client.SshClient;

public class SshClientUtils {
    private static SshClient client = null;

    public static SshClient getSshClient() {
        if (client == null) {
            client = SshClient.setUpDefaultClient();
            client.start();
        }
        return client;
    }
}
