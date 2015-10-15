package tests.support

import client.HttpClient
import com.google.common.io.Files

import static Exec.exec

class WireMockOnHeroku {

    static void setup(String appName) {
        def client = new HttpClient("http://${appName}.herokuapp.com/")
        if (client.get("/__admin/").statusCode() == 404) {
            def tempdir = Files.createTempDir()
            tempdir.deleteOnExit()
            exec(tempdir, "git clone git@github.com:Mahoney/wiremock-heroku.git")
            exec(new File(tempdir, 'wiremock-heroku'), "./wiremock-heroku.sh $appName")
            exec("heroku domains:add ${appName}.global.ssl.fastly.net --app $appName")

            Runtime.runtime.addShutdownHook {
                exec("heroku apps:destroy --app $appName --confirm $appName")
            }
        }
    }
}
