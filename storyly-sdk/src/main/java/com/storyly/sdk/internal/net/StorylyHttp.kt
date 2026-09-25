package com.storyly.sdk.internal.net

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * One OkHttp client for the whole SDK, shared by the API calls and the image
 * loader so they pool connections instead of opening two separate stacks.
 */
internal object StorylyHttp {

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
