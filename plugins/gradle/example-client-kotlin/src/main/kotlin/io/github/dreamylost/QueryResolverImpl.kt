package io.github.dreamylost

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest
import io.github.dreamylost.api.QueryResolver
import io.github.dreamylost.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Server example at https://github.com/jxnu-liguobin/springboot-examples/tree/master/graphql-complete
 * (only a normal graphql server implement by java )
 * @author 梦境迷离
 * @version 1.0,2020/12/14
 */
class QueryResolverImpl : QueryResolver {

    override fun hero(episode: EpisodeTO?): CharacterTO? {
        val heroQueryRequest = HeroQueryRequest()
        heroQueryRequest.setEpisode(episode)
        val characterResponseProjection = CharacterResponseProjection().`all$`(3)
        val graphQLRequest = GraphQLRequest(heroQueryRequest, characterResponseProjection)
        val ret = getResponse<HeroQueryResponse>(graphQLRequest)
        return ret.hero()
    }

    override fun human(id: String?): HumanTO? {
        val humanQueryRequest = HumanQueryRequest()
        humanQueryRequest.setId(id)
        val humanResponseProjection = HumanResponseProjection().`all$`(1)
        val graphQLRequest = GraphQLRequest(humanQueryRequest, humanResponseProjection)
        val ret = getResponse<HumanQueryResponse>(graphQLRequest)
        return ret.human()
    }

    override fun humans(): List<HumanTO?>? {
        val humanQueryRequest = HumansQueryRequest()
        val humanResponseProjection = HumanResponseProjection().`all$`(1)
        val graphQLRequest = GraphQLRequest(humanQueryRequest, humanResponseProjection)
        val ret = getResponse<HumansQueryResponse>(graphQLRequest)
        return ret.humans()
    }

    override fun droid(id: String): DroidTO? {
        val productByIdQueryRequest = DroidQueryRequest()
        productByIdQueryRequest.setId(id)
        val droidResponseProjection = DroidResponseProjection().`all$`(1)
        val graphQLRequest = GraphQLRequest(productByIdQueryRequest, droidResponseProjection)
        val ret = getResponse<DroidQueryResponse>(graphQLRequest)
        return ret.droid()
    }
}

inline fun <reified T> getResponse(request: GraphQLRequest, url: String = "http://localhost:8080/graphql"): T {
    val json = "application/json; charset=utf-8".toMediaTypeOrNull()
    val body = request.toHttpJsonBody().toRequestBody(json)
    val client = OkHttpClient()
    val request = Request.Builder().post(body).url(url).build()
    val call = client.newCall(request)
    return Jackson.mapper.readValue<T>(call.execute().body!!.string())
}

object Jackson {
    val mapper = jsonMapper {
        addModule(kotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        serializationInclusion(JsonInclude.Include.NON_ABSENT)
        serializationInclusion(JsonInclude.Include.NON_NULL)
    }

}