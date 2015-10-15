package client;

import com.google.common.collect.ImmutableMap;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static java.util.stream.IntStream.range;

public class HttpClient {

    private final URI baseUri;
    private final ImmutableMap<String, String> standardHeaders;

    public HttpClient(String baseUri) {
        this(baseUri, ImmutableMap.<String, String>of());
    }

    public HttpClient(String baseUri, ImmutableMap<String, String> standardHeaders) {
        this.baseUri = URI.create(baseUri);
        this.standardHeaders = standardHeaders;
    }

    public HttpResponse get(String path) {
        return get(path, ImmutableMap.of());
    }

    public HttpResponse get(String path, Map<String, String> headers) {
        return execute("GET", path, headers);
    }

    public HttpResponse post(String path) {
        return post(path, ImmutableMap.of());
    }

    public HttpResponse post(String path, String body) {
        return post(path, ImmutableMap.of(), body);
    }

    public HttpResponse post(String path, Map<String, String> headers) {
        return post(path, headers, null);
    }

    public HttpResponse post(String path, Map<String, String> headers, String body) {
        return execute("POST", path, headers, body);
    }

    public HttpResponse execute(String method, String path) {
        return execute(method, path, ImmutableMap.of());
    }

    public HttpResponse execute(String method, String path, Map<String, String> headers) {
        return execute(method, path, headers, null);
    }

    public HttpResponse execute(String method, String path, Map<String, String> headers, String body) {

        HttpRequest request = new HttpRequest().method(method).set(baseUri.resolve(path).toASCIIString());

        request.timeout((int) ofSeconds(5).toMillis());

        standardHeaders.entrySet().stream().forEach(entry ->
                request.header(entry.getKey(), entry.getValue())
        );
        headers.entrySet().stream().forEach(entry ->
                request.header(entry.getKey(), entry.getValue())
        )
        ;
        if (body != null) {
            request.body(body);
        }

        HttpResponse response = request.send();
        System.out.println(
                response.statusCode() + " " + response.statusPhrase() + " in response to " + method + " " + response.getHttpRequest().url() + '\n' +
                header(response, "Cache-Control") +
                header(response, "X-Served-By") +
                header(response, "X-Cache") +
                header(response, "X-Cache-Hits") +
                header(response, "Fastly-Debug-Path") +
                header(response, "Fastly-Debug-Ttl") +
                header(response, "Fastly-Debug-Digest")
        );
        return response;
    }

    private String header(HttpResponse response, String headerName) {
        return headerName + ": " + Arrays.toString(response.headers(headerName)) + '\n';
    }

    public void warm(String path) {
        range(0, 100).parallel().forEach(n -> {
            try {
                get(path);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public HttpResponse put(String path) {
        return execute("PUT", path);
    }

    public HttpResponse delete(String path) {
        return execute("DELETE", path);
    }
}
