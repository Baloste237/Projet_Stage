package com.example.backend.scan.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class MobSFConfig {


    private final MobSFProperties props;

    public MobSFConfig(MobSFProperties props) {
        this.props = props;
    }

    @Bean
    public WebClient mobsfWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(props.getReadTimeout()))
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        props.getConnectTimeout() * 1000
                );

        return WebClient.builder()
                .baseUrl(props.getUrl())
                .defaultHeader("Authorization", props.getApiKey())
                .defaultHeader("X-Mobsf-Api-Key", props.getApiKey())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(c -> c.defaultCodecs()
                        .maxInMemorySize(50 * 1024 * 1024))  // 50 MB max
                .build();
    }
}
