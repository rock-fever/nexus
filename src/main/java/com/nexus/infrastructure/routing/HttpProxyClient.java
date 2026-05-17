package com.nexus.infrastructure.routing;

import java.util.Collections;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpProxyClient {

    // Headers that must not be forwarded to the upstream service.
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "transfer-encoding", "te",
            "trailer", "upgrade", "proxy-authorization", "proxy-authenticate", "host"
    );

    private final RestClient restClient;

    public HttpProxyClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("null")
    public void forward(HttpServletRequest request, HttpServletResponse response, String targetUrl)
            throws Exception {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        byte[] requestBody = request.getInputStream().readAllBytes();

        RestClient.RequestBodySpec spec = restClient
                .method(method)
                .uri(targetUrl)
                .headers(headers ->
                        Collections.list(request.getHeaderNames()).forEach(name -> {
                            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                                headers.addAll(name, Collections.list(request.getHeaders(name)));
                            }
                        })
                )
                .header("X-Forwarded-For", request.getRemoteAddr())
                .header("X-Forwarded-Host", request.getServerName())
                .header("X-Forwarded-Proto", request.getScheme());

        ResponseEntity<byte[]> upstream = (requestBody.length > 0 ? spec.body(requestBody) : spec)
                .retrieve()
                // Pass 4xx/5xx responses through as-is rather than throwing exceptions.
                .onStatus(HttpStatusCode::isError, (req, res) -> {})
                .toEntity(byte[].class);

        response.setStatus(upstream.getStatusCode().value());
        upstream.getHeaders().forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                values.forEach(value -> response.addHeader(name, value));
            }
        });

        byte[] body = upstream.getBody();
        if (body != null && body.length > 0) {
            response.getOutputStream().write(body);
        }
    }
}
