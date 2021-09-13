package com.pradale.kterm.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

public class KTermConsole extends OutputStream {
    private TextArea textArea;

    public KTermConsole(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int value) throws IOException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textArea.appendText(String.valueOf((char) value));
            }
        });
    }
}
