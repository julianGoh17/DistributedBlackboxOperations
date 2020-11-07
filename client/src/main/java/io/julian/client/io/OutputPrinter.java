package io.julian.client.io;

// Created OutputPrinter class to easily be able to mock printing to command line in unit tests
public class OutputPrinter {
    public void println(final String string) {
        System.out.println(string);
    }
}
