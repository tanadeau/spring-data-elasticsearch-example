package com.example.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.net.HostAndPort
import mu.KLogging
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.cluster.ClusterName
import org.elasticsearch.common.settings.Settings.settingsBuilder
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.EntityMapper
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.io.IOException
import java.net.InetAddress

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapperBuilder() = Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())
}

@Configuration
@Import(BeanValidatorPluginsConfiguration::class)
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
                .title("Test API")
                .description("API docs for Test API")
                .version("0.0.1")
                .build()
    }
}

@Configuration
@EnableElasticsearchRepositories("com.example.repository")
class ESConfig {
    companion object : KLogging()

    @Value("\${elastic.local}")
    private var local: Boolean = false

    @Value("\${elastic.cluster}")
    private lateinit var cluster: String

    @Value("\${elastic.hosts}")
    private lateinit var hosts: String

    /**
     * This manner won't allow GBSP to be started up before elastic ...
     * However, it does allow to reconnect if elastic goes down first.
     */
    @Bean
    fun client(): Client {
        val esClient: Client
        if (local) {
            val settings = settingsBuilder().put("path.home", "data")
            esClient = nodeBuilder().settings(settings).local(true).node().client()
        } else {
            val settings = settingsBuilder().put(ClusterName.SETTING, cluster).build()
            esClient = TransportClient.builder().settings(settings).build()

            hosts.split(',').map {
                val hostAndPort = HostAndPort.fromString(it).withDefaultPort(9300)
                val inetAddress = InetAddress.getByName(hostAndPort.host)
                InetSocketTransportAddress(inetAddress, hostAndPort.port)
            }.forEach {
                logger.info { "Adding transport address ${it.host}:${it.port}" }
                esClient.addTransportAddress(it)
            }

            // verify connection
            val connectedNodes = esClient.connectedNodes()
            if (connectedNodes.isEmpty()) {
                esClient.close()
                throw IOException("Could not connect to any Elasticsearch nodes[$hosts]")
            }
        }

        return esClient
    }

    @Bean
    fun entityMapper(objectMapper: ObjectMapper): EntityMapper = ElasticsearchEntityMapper(objectMapper)

    @Bean
    fun elasticsearchTemplate(client: Client, mapper: EntityMapper) = ElasticsearchTemplate(client, mapper)
}

internal class ElasticsearchEntityMapper(val objectMapper: ObjectMapper) : EntityMapper {
    override fun <T : Any?> mapToObject(source: String?, clazz: Class<T>?): T {
        return objectMapper.readValue(source, clazz)
    }

    override fun mapToString(`object`: Any?): String {
        return objectMapper.writeValueAsString(`object`)
    }
}