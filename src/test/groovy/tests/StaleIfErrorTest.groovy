package tests

import spock.lang.Unroll

import static java.util.concurrent.TimeUnit.SECONDS

class StaleIfErrorTest extends WiremockCdnTest {

    @Unroll
    def 'when origin broken and #invalidationActionName, #cacheSettings returns #expectedResponse'() {

        given:
            origin.get(path).returns(
                'Surrogate-Key': path,
                (CacheControl): "max-age=$secondsToStale,$cacheSettings",
                'cached response'
            )

        and:
            cdn.warm(path)

        when:
            origin
                .get(path)
                .returns(500, "broken response")

        and:
            invalidationAction.call(path)

        then:
            cdn.get(path).body() == expectedResponse

        where:
            invalidationActionName  | invalidationAction                               | cacheSettings                                                               | expectedResponse
            'cache soft purged'     | { fastly.softPurgeByKey(it)}                     | ''                                                                          | 'broken response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}                     | "stale-while-revalidate=${secondsToStale}"                                  | 'broken response'
            'cache soft purged'     | { fastly.softPurgeByKey(it) && SECONDS.sleep(3)} | 'stale-while-revalidate=1,stale-if-error=1'                                 | 'broken response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}                     | "stale-if-error=${secondsToStale}"                                          | 'cached response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}                     | "stale-while-revalidate=${secondsToStale},stale-if-error=${secondsToStale}" | 'cached response'

            'cache hard purged'     | { fastly.purgeByKey(it)}                         | ''                                                                          | 'broken response'
            'cache hard purged'     | { fastly.purgeByKey(it)}                         | "stale-while-revalidate=${secondsToStale}"                                  | 'broken response'
            'cache hard purged'     | { fastly.purgeByKey(it) && SECONDS.sleep(3) }    | 'stale-while-revalidate=1,stale-if-error=1'                                 | 'broken response'
            'cache hard purged'     | { fastly.purgeByKey(it)}                         | "stale-if-error=${secondsToStale}"                                          | 'broken response'
            'cache hard purged'     | { fastly.purgeByKey(it)}                         | "stale-while-revalidate=${secondsToStale},stale-if-error=${secondsToStale}" | 'broken response'

            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }              | ''                                                                          | 'broken response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }              | "stale-while-revalidate=${secondsToStale}"                                  | 'broken response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+3) }              | 'stale-while-revalidate=1,stale-if-error=1'                                 | 'broken response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }              | "stale-if-error=${secondsToStale}"                                          | 'cached response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }              | "stale-while-revalidate=${secondsToStale},stale-if-error=${secondsToStale}" | 'cached response'

    }
}
