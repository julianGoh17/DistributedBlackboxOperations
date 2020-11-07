package io.julian.client.io;

import java.util.Scanner;

// Created InputReader class to easily be able to mock Command Line inputs in unit tests
public class InputReader {
    private static final Scanner SCANNER = new Scanner(System.in);

    public String nextLine() {
        return SCANNER.nextLine();
    }
}
