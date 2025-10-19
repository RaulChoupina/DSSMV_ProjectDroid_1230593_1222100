package com.example.projdroid.data;

import java.util.HashMap;
import java.util.Map;

public final class ClientData {

    // email -> password
    private static final Map<String, String> CLIENTS = new HashMap<>();

    static {
        CLIENTS.put("cliente1@gmail.com", "1234");
        CLIENTS.put("cliente2@gmail.com", "abcd");
        CLIENTS.put("cliente3@gmail.com", "senha");
    }

    public static boolean validar(String email, String password) {
        if (email == null || password == null) return false;
        String saved = CLIENTS.get(email);
        return saved != null && saved.equals(password);
    }

    private ClientData() {} // evitar instância
}
