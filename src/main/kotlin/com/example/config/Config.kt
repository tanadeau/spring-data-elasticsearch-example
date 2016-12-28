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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices
import org.springframework.boot.context.embedded.FilterRegistrationBean
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
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.context.SecurityContextPersistenceFilter
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.net.InetAddress
import javax.servlet.Filter

@Configuration
class JacksonConfig {
    @Bean
    open fun objectMapperBuilder() = Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())
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
@EnableOAuth2Client
class OAuthConfig : WebSecurityConfigurerAdapter(false) {
    companion object : KLogging()

    @Suppress("SpringKotlinAutowiring")
    @Autowired
    var oauth2ClientContext: OAuth2ClientContext? = null


    @Bean
    @ConfigurationProperties("clientSpec")
    fun clientSpec(): ClientResources {
        return ClientResources()
    }

    private fun ssoFilter(): Filter {
        val cs = clientSpec()
        val filter = OAuth2ClientAuthenticationProcessingFilter(
                "/login/${cs.url}")
        val oAuth2RestTemplate = OAuth2RestTemplate(cs.client, oauth2ClientContext)
        filter.setRestTemplate(oAuth2RestTemplate)
        filter.setTokenServices(
                UserInfoTokenServices(cs.resource.userInfoUri, cs.client.clientId))

        // example of doing something when the authenication was successful
        filter.setAuthenticationSuccessHandler { httpServletRequest, httpServletResponse, authentication ->
            logger.info { "Logged Name = ${authentication.name}" }
        }
        return filter
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        // @formatter:off
        http
                // XXX ** didn't seem to stop the cookie -- .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/webjars/**")
                .permitAll().anyRequest()
                .authenticated()
                .and().exceptionHandling()
                .authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/login/github"))
                .and()
                .logout()
                .logoutSuccessUrl("/").permitAll().and().csrf().disable()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter::class.java)
        // @formatter:on
    }
}

class ClientResources {

    @NestedConfigurationProperty
    val client = AuthorizationCodeResourceDetails()

    @NestedConfigurationProperty
    val resource = ResourceServerProperties()

    var url: String = ""
}