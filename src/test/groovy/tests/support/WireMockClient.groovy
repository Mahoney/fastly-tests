package tests.support

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.RequestMethod
import tests.support.OngoingStubbing

class WireMockClient {

    final WireMock wiremock

    WireMockClient(WireMock wiremock) {
        this.wiremock = wiremock
    }

    OngoingStubbing get(String path) {
        return execute(path, RequestMethod.GET)
    }

    OngoingStubbing head(String path) {
        return execute(path, RequestMethod.HEAD)
    }

    OngoingStubbing execute(String path, RequestMethod method) {
        return new OngoingStubbing(wiremock, new MappingBuilder(method, WireMock.urlEqualTo(path)))
    }

    void broken(String path) {
        wiremock.register(WireMock.any(WireMock.urlEqualTo(path)).willReturn(WireMock.aResponse().withFault(Fault.EMPTY_RESPONSE)))
    }

    void reset() {
        wiremock.resetMappings()
    }
}
