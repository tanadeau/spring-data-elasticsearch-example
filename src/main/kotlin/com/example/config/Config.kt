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
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.EntityMapper
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.jwt.crypto.sign.RsaVerifier
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore
import java.io.IOException
import java.net.InetAddress

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder = Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())
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


@Configuration
@EnableResourceServer
@EnableWebSecurity
class OAuth2ResourceConfig : ResourceServerConfigurerAdapter() {
    companion object : KLogging()

    override fun configure(config: ResourceServerSecurityConfigurer) {
        config.tokenServices(tokenServices())
        config.resourceId(null)
    }

    @Bean
    @ConfigurationProperties("clientSpec")
    fun clientSpec(): ClientResources {
        return ClientResources()
    }

    @Bean
    fun tokenStore(): TokenStore {
        return JwtTokenStore(accessTokenConverter())
    }

    @Bean
    fun accessTokenConverter(): JwtAccessTokenConverter {
        val converter = JwtAccessTokenConverter()
        if (clientSpec().resource.jwt?.keyValue == null) {
            // This is in here until you want to just use a constant keycloak server and its public key
            // Should remove this, once that happens
            logger.warn { "Skipping verifier of oauth server" }
            converter.setVerifier(NoOpVerifier())
        } else {
            converter.setVerifier(RsaVerifier(decodePublicKey(clientSpec().resource.jwt.keyValue)))
        }
        return converter
    }

    @Bean
    fun tokenServices(): DefaultTokenServices {
        val defaultTokenServices = DefaultTokenServices()
        defaultTokenServices.setTokenStore(tokenStore())
        return defaultTokenServices
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
        http.authorizeRequests().anyRequest().authenticated()
    }
}

class ClientResources {
    @NestedConfigurationProperty
    val client = AuthorizationCodeResourceDetails()

    @NestedConfigurationProperty
    val resource = ResourceServerProperties()
}