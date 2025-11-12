package com.example.projdroid.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String DEFAULT_BASE_URL = "http://193.136.62.24/v1/";
    private static Retrofit retrofit;
    private static String lastBaseUrl;
    private static LibraryApi apiService;

    /** Retrofit com a base URL por defeito */
    public static Retrofit getClient() {
        return getClient(DEFAULT_BASE_URL);
    }

    /** Cria/recicla Retrofit garantindo que a baseUrl termina em "/" */
    public static synchronized Retrofit getClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) baseUrl = DEFAULT_BASE_URL;
        if (!baseUrl.endsWith("/")) baseUrl += "/";

        if (retrofit == null || !baseUrl.equals(lastBaseUrl)) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            lastBaseUrl = baseUrl;
            apiService = null; // força recriar o serviço se a base mudar
        }
        return retrofit;
    }

    /** Atalho: devolve o serviço LibraryApi pronto a usar com a base por defeito */
    public static synchronized LibraryApi get() {
        if (apiService == null) {
            apiService = getClient().create(LibraryApi.class);
        }
        return apiService;
    }
}
