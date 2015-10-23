package client;

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic;
import jodd.http.HttpResponse;

import static com.google.common.collect.ImmutableMap.of;

@CompileStatic
public class FastlyClient {

    private final HttpClient client;
    private final String serviceId;

    public FastlyClient(String serviceId, String apiKey) {
        this.serviceId = serviceId;
        this.client = new HttpClient("https://api.fastly.com/", of("Fastly-Key", apiKey));
    }

    public HttpResponse purgeAll() {
        return ifSuccess(
            client.post("/service/" + serviceId + "/purge_all")
        );
    }

    public HttpResponse softPurgeByKey(String key) {
        return ifSuccess(
            client.post(purgeUrl(key), of("Fastly-Soft-Purge", "1"))
        );
    }

    public HttpResponse purgeByKey(String key) {
        return ifSuccess(
            client.post(purgeUrl(key))
        );
    }

    public List<Map<String, Object>> services() {
        return json(ifSuccess(client.get("/service")), List.class);
    }

    private <T> T json(HttpResponse response, Class<T> type) {
        try {
            return new ObjectMapper().readValue(response.body(), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String purgeUrl(String key) {
        return "/service/"+serviceId+"/purge/"+key;
    }

    private HttpResponse ifSuccess(HttpResponse response) {
        assert response.statusCode() < 400, "Not a success: \n"+response+" in response to "+response.getHttpRequest()
        return response
    }


    public Map createService(String name, String backend) {
        Map service = json(ifSuccess(client.post("/service", "name=" + name)), Map.class);
        String id = service.get("id").toString();
        ifSuccess(client.post("/service/"+ id +"/version/1/domain", "name="+name+".global.ssl.fastly.net"));
        ifSuccess(client.post("/service/" + id + "/version/1/backend", "hostname=" + backend + "&name=" + name));
        ifSuccess(client.put("/service/" + id + "/version/1/activate"));
        return service;
    }

    public void destroyService(String id) {
        ifSuccess(client.put("/service/" + id + "/version/1/deactivate"));
        ifSuccess(client.delete("/service/" + id));
    }
}
