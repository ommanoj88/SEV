package com.evfleet.charging.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry Configuration for Distributed Tracing
 * Integrates with Jaeger/OTLP for observability
 * NOTE: Temporarily disabled - requires opentelemetry-semconv and related dependencies
 * To enable: Add OpenTelemetry dependencies to pom.xml and uncomment this configuration
 */
//@Configuration
public class OpenTelemetryConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;

    // Commented out until OpenTelemetry dependencies are added
    /*
    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.of(
                                ResourceAttributes.SERVICE_NAME, serviceName,
                                ResourceAttributes.SERVICE_VERSION, "1.0.0",
                                ResourceAttributes.DEPLOYMENT_ENVIRONMENT, "development"
                        )
                ));

        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setResource(resource)
                .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(JaegerPropagator.getInstance()))
                .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::close));

        return openTelemetrySdk;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, "1.0.0");
    }
    */
}
