package tests

import spock.lang.Unroll

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic

class SurrogateKeyTest extends WiremockCdnTest {

    static def commonKey = 'common-key'

    String path1
    String path2

    def setup() {
        path1 = '/'+randomAlphabetic(10)
        path2 = '/'+randomAlphabetic(10)
    }

    @Unroll
    def 'Purge #purgeName results in returning #expectedPath1Response for path 1 and #expectedPath2Response for path 2'() {

        given:
            origin.get(path1).returns(
                'Surrogate-Key': "$path1 $commonKey",
                "cached response to $path1"
            )

            origin.get(path2).returns(
                'Surrogate-Key': "$path2 $commonKey",
                "cached response to $path2"
            )

        and:
            cdn.warm(path1)
            cdn.warm(path2)

        when:
            origin.get(path1).returns(
                "new response to $path1"
            )
            origin.get(path2).returns(
                "new response to $path2"
            )

        and:
            purgeAction.call(path1, path2)

        then:
            cdn.get(path1).body() == "$expectedPath1Response to $path1".toString()
            cdn.get(path2).body() == "$expectedPath2Response to $path2".toString()

        where:
        purgeName            | purgeAction                                          | expectedPath1Response | expectedPath2Response
        'none'               | ({path1, path2 -> /* purge nothing*/})               | 'cached response'     | 'cached response'
        'path 1'             | ({path1, path2 -> fastly.purgeByKey(path1)})         | 'new response'        | 'cached response'
        'soft of path 1'     | ({path1, path2 -> fastly.softPurgeByKey(path1)})     | 'new response'        | 'cached response'
        commonKey            | ({path1, path2 -> fastly.purgeByKey(commonKey)})     | 'new response'        | 'new response'
        "soft of $commonKey" | ({path1, path2 -> fastly.softPurgeByKey(commonKey)}) | 'new response'        | 'new response'
    }
}
