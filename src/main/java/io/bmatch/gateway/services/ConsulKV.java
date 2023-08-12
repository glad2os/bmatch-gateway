package io.bmatch.gateway.services;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import org.springframework.stereotype.Service;

@Service
public class ConsulKV {
    private final ConsulClient consulClient;

    public ConsulKV(ConsulClient consulClient) {
        this.consulClient = consulClient;
    }

    public boolean setKeyValue(String key, String value) {
        PutParams putParams = new PutParams();

        Response<Boolean> kvResponse = consulClient.setKVValue(key, value, putParams);
        return kvResponse.getValue();
    }

    public String getKeyValue(String key) {
        Response<GetValue> kvResponse = consulClient.getKVValue(key);
        GetValue value = kvResponse.getValue();

        if (value == null) {
            return null;
        } else {
            return value.getDecodedValue();
        }
    }
}
