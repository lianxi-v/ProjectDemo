package yjj;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

@SpringBootTest(classes = {ApiApplication.class})
@RunWith(SpringRunner.class)
public class EccangAPI {

    public final static String URL = "https://openapi-web.eccang.com/openApi/api/unity";
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testOne() {
        String serviceId = "ES43CE";
        String appKey = "dbfb74b920d2443f";
        String secretKey = "82a6aee273e93f58";
        String interfaceMethod = "ListingPerformance";
        String bizContent = "{\\\"page\\\":1,\\\"page_size\\\":20}";
        String charset = "UTF-8";
        String nonceStr = "esetol";
        String signType = "AES";
        long ts = System.currentTimeMillis();
        String version = "v1.0.0";

        String signStr = "app_key=" + appKey +
                "&biz_content=" + bizContent +
                "&charset=" + charset +
                "&interface_method=" + interfaceMethod +
                "&nonce_str=" + nonceStr +
                "&service_id=" + serviceId +
                "&sign_type=" + signType +
                "&timestamp=" + ts +
                "&version=" + version;

        JSONObject erpParam = new JSONObject();
        erpParam.put("service_id", serviceId);
        erpParam.put("app_key", appKey);
        erpParam.put("interface_method", interfaceMethod);
        erpParam.put("biz_content", bizContent);
        erpParam.put("charset", charset);
        erpParam.put("nonce_str", nonceStr);
        erpParam.put("sign_type", signType);
        erpParam.put("sign", this.encrypt(secretKey, signStr));
        erpParam.put("timestamp", ts);
        erpParam.put("version", version);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(URL);
        post.addHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(JSONObject.toJSONString(erpParam), "UTF-8"));
        try {
            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("111111");
                return;
            }
            HttpEntity resEntity = response.getEntity();
            String resEntityData = EntityUtils.toString(resEntity);
            JSONObject result = JSONObject.parseObject(resEntityData);
            if (null == result) {
                System.out.println("2222");
                return;
            }
            if (!"200".equals(result.getString(""))) {
                System.out.println(JSONObject.toJSONString(result));
            }
            JSONObject biz_content = JSONObject.parseObject(result.getString("biz_content"));
            System.out.println(JSONObject.toJSONString(biz_content));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("4444444");
        }
    }

    public String encrypt(String secretKey, String content) {
        byte[] result = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec("1234500000054321".getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec);
            result = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("55555");
        }
        return Base64.encodeBase64String(result);
    }

}

