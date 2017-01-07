package com.example.config

import com.example.model.Account
import com.example.repository.AccountRepository
import com.example.service.AccountService
import com.example.stomp.RequestHandshakeMessageInterceptor
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
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.adapters.spi.KeycloakAccount
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.keycloak.representations.adapters.config.AdapterConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.EntityMapper
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
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
    fun objectMapperBuilder() = Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())!!
}

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
class WebConfig

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

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = arrayOf(KeycloakSecurityComponents::class))
class KeycloakSecurityConfig(private val accountService: AccountService) : KeycloakWebSecurityConfigurerAdapter() {
    private val kcDeployment: KeycloakDeployment by lazy {
        KeycloakDeploymentBuilder.build(keycloakAdapterConfig())!!
    }

    /**
     * Registers the KeycloakAuthenticationProvider with the authentication manager.
     */
    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        val authorityMapper = SimpleAuthorityMapper().apply { setConvertToUpperCase(true) }
        val kcAuthProvider = keycloakAuthenticationProvider().apply { setGrantedAuthoritiesMapper(authorityMapper) }
        auth.authenticationProvider(kcAuthProvider)
    }

    @Bean
    override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return NullAuthenticatedSessionStrategy()
    }

    override fun keycloakAuthenticationProvider(): KeycloakAuthenticationProvider {
        return KeycloakAccountAuthenticationProvider(accountService)
    }

    override fun configure(http: HttpSecurity) {
        super.configure(http)

        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/post/**").authenticated()
                .antMatchers("/account/**").authenticated()
                .antMatchers("/websocket/**").permitAll()
                .anyRequest().permitAll().and()
                .csrf().disable()
    }

    @Bean
    @ConfigurationProperties("keycloak")
    fun keycloakAdapterConfig(): AdapterConfig {
        return AdapterConfig()
    }

    @Bean
    fun configResolver() = KeycloakConfigResolver { kcDeployment }
}

/**
 *   Help on setting up a full broker: (scaling up websockets)
 *      https://raymondhlee.wordpress.com/2014/04/12/set-up-a-full-broker-for-spring-4-stomp-over-websocket-messaging-using-activemq/
 */

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : AbstractWebSocketMessageBrokerConfigurer() {
    companion object : KLogging()
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        //config.enableStompBrokerRelay("/topic").setRelayHost("host").setRelayPort(1000).setSystemHeartbeatReceiveInterval(12)
        config.enableSimpleBroker("/topic/activity")
        config.setApplicationDestinationPrefixes("/gbsp")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/websocket")
    }
}

@Configuration
class WebSocketSecurityConfig(val accountRepository: AccountRepository) : AbstractSecurityWebSocketMessageBrokerConfigurer() {
    companion object : KLogging()
    override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry) {
        messages
                .anyMessage().permitAll()
    }

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.setInterceptors(RequestHandshakeMessageInterceptor(accountRepository))
    }

    override fun sameOriginDisabled(): Boolean {
        return true
    }
}

private class ElasticsearchEntityMapper(private val objectMapper: ObjectMapper) : EntityMapper {
    override fun <T : Any?> mapToObject(source: String?, clazz: Class<T>?): T {
        return objectMapper.readValue(source, clazz)
    }

    override fun mapToString(`object`: Any?): String {
        return objectMapper.writeValueAsString(`object`)
    }
}

private class KeycloakAccountAuthenticationProvider(private val accountService: AccountService) :
        KeycloakAuthenticationProvider() {
    override fun authenticate(authentication: Authentication?): Authentication? {
        val kcToken = super.authenticate(authentication) as KeycloakAuthenticationToken? ?: return null
        val kcPrincipal = kcToken.principal as KeycloakPrincipal<*>
        val username = kcPrincipal.keycloakSecurityContext.token.preferredUsername ?: return null

        val account = accountService.findByUsername(username) ?:
                throw UsernameNotFoundException("Could not find account with username $username")

        return KeycloakAccountAuthenticationToken(account, kcToken.account, kcToken.authorities)
    }
}

class KeycloakAccountAuthenticationToken(
        val domainAccount: Account, kcAccount: KeycloakAccount, authorities: Collection<GrantedAuthority>) :
        KeycloakAuthenticationToken(kcAccount, authorities) {
    override fun getPrincipal() = domainAccount
}