package ru.axel.yamobdev2018.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.axel.yamobdev2018.BuildConfig;

public class ServiceGenerator {
    private static final String BASE_URL = "https://api.imgur.com/3/";
    private static final String CLIENT_ID = "Client-ID " + BuildConfig.IMGUR_CLIENT_ID;

    private static Retrofit.Builder sBuilder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create());


    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    .header("Authorization", CLIENT_ID)
//                                    .method()
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    .addInterceptor(logging);

    private static Retrofit sRetrofit = sBuilder
            .client(httpClient.build())
            .build();

    public static <S> S createService(Class<S> serviceClass) {
        return sRetrofit.create(serviceClass);
    }
}
