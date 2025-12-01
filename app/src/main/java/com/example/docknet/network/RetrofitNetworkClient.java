package com.example.docknet.network;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

import java.io.IOException;

public class RetrofitNetworkClient implements NetworkClient {
    interface Api {
        @GET
        Call<String> getRaw(@Url String url);
    }

    private final Api api;
    private volatile Call<String> currentCall = null;

    public RetrofitNetworkClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(java.time.Duration.ofSeconds(15))
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .readTimeout(java.time.Duration.ofSeconds(15))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://example.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build();

        api = retrofit.create(Api.class);
    }

    @Override
    public String performApiRequest(String urlString) throws IOException {
        Call<String> call = api.getRaw(urlString);
        synchronized (this) {
            currentCall = call;
        }
        try {
            Response<String> resp = call.execute();
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new IOException("HTTP error: " + resp.code());
            }
            return resp.body();
        } finally {
            synchronized (this) {
                if (currentCall == call) currentCall = null;
            }
        }
    }

    @Override
    public void cancelCurrentRequest() {
        synchronized (this) {
            if (currentCall != null && !currentCall.isCanceled()) {
                currentCall.cancel();
                currentCall = null;
            }
        }
    }
}
