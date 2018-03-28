package com.ics.mapslearn.network;

import com.ics.mapslearn.response.Response;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Ichsan.Fatiha on 3/26/2018.
 */

public interface APIServices {
    @GET("json")
    Call<Response> request_route(@Query("origin") String origin,
                                 @Query("destination") String destination,
                                 @Query("api_key") String api_key);
}
