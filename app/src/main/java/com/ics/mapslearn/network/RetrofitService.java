package com.ics.mapslearn.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ichsan.Fatiha on 3/27/2018.
 */

public class RetrofitService {
    public static final String url = "https://maps.googleapis.com/maps/api/directions/";
    private static Retrofit retrofit = null;

    public static Retrofit service(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
