package com.example.projdroid.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public final class ClientData {

    private static final String PREF_NAME = "client_data";
    private static final String KEY_CLIENTS = "clients_json";
    private static final Gson gson = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private ClientData() {}

    // ---------- Utilizadores internos ----------

    private static Map<String, String> load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_CLIENTS, null);
        Map<String, String> map = gson.fromJson(json, TYPE);
        return (map != null) ? map : new HashMap<>();
    }

    private static void save(Context ctx, Map<String, String> map) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_CLIENTS, gson.toJson(map)).apply();
    }

    // ---------- API pública ----------

    /** Regista um novo cliente, devolve false se já existir ou for inválido. */
    public static boolean registar(Context ctx, String email, String password) {
        if (email == null || password == null) return false;
        email = email.trim().toLowerCase();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return false;

        Map<String, String> clients = load(ctx);
        if (clients.containsKey(email)) return false; // já existe
        clients.put(email, password);
        save(ctx, clients);
        return true;
    }

    /** Valida login. */
    public static boolean validar(Context ctx, String email, String password) {
        if (email == null || password == null) return false;
        email = email.trim().toLowerCase();
        Map<String, String> clients = load(ctx);
        String saved = clients.get(email);
        return saved != null && saved.equals(password);
    }

    /** Remove uma conta (útil em testes). */
    public static boolean remover(Context ctx, String email) {
        if (email == null) return false;
        email = email.trim().toLowerCase();
        Map<String, String> clients = load(ctx);
        if (!clients.containsKey(email)) return false;
        clients.remove(email);
        save(ctx, clients);
        return true;
    }

    /** Pré-carrega clientes de exemplo na primeira execução. */
    public static void preload(Context ctx) {
        Map<String, String> clients = load(ctx);
        if (clients.isEmpty()) {
            clients.put("cliente1@gmail.com", "1234");
            clients.put("cliente2@gmail.com", "abcd");
            clients.put("cliente3@gmail.com", "senha");
            save(ctx, clients);
        }
    }
}
