package tests.support

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock

import java.time.Duration

class OngoingStubbing {

    private final WireMock wiremock
    private final MappingBuilder mappingBuilder
    private ResponseDefinitionBuilder response = WireMock.aResponse()

    OngoingStubbing(WireMock wiremock, MappingBuilder mappingBuilder) {
        this.wiremock = wiremock
        this.mappingBuilder = mappingBuilder
    }

    OngoingStubbing after(Duration duration) {
        response.withFixedDelay((int) duration.toMillis())
        this
    }

    void returns(Map<String, String> headers = [:], String body) {

        returns(200, headers, body)
    }

    void returns(int responseCode, Map<String, String> headers = [:], String body) {

        response.withStatus(responseCode)

        if (body) {
            response.withBody(body)
        }

        headers.each { k, v ->
            response.withHeader(k, v)
        }

        wiremock.register(mappingBuilder.willReturn(response))
    }
}
