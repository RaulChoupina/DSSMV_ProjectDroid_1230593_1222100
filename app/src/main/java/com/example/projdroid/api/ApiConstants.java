package com.example.projdroid.api;

public class ApiConstants {

    // O URL base do teu servidor para os assets
    private static final String ASSET_BASE_URL = "http://193.136.62.24/v1/assets/cover/";

    /**
     * Constrói o URL completo para uma imagem de capa.
     * @param imageName O nome do ficheiro da imagem (ex: "isbn.jpg" ou "nome_unico.png")
     * @return O URL completo, ou null se o nome da imagem for inválido.
     */
    public static String coverUrl(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return null; // Retorna nulo se não houver nome de imagem
        }

        // Evita barras duplicadas se o imageName já vier com uma
        if (imageName.startsWith("/")) {
            imageName = imageName.substring(1);
        }

        return ASSET_BASE_URL + imageName;
    }
}