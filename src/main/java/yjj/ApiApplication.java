package yjj;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;


@SpringBootApplication
@RestController

public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    //封装成参数
    public final static String URL = "https://openapi-web.eccang.com/openApi/api/unity";
    private static final String SERVICE_ID = "ES43CE";
    private static final String API_KEY = "2926506cd6d145ab";
    private static final String API_SECRET = "be9302cf16dff020";
    private static final String SIGN_TYPE = "AES";
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String NONCE_STR = "esetol";


    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    @PostMapping("/api")
    public ResponseEntity<String> callApi(@RequestBody Person person) {

        long timestamp = System.currentTimeMillis();
        try {

            //参数拼接
            String signStr = "app_key=" + API_KEY +
                    "&biz_content=" + person.biz_content +
                    "&charset=" + person.charset +
                    "&interface_method=" + person.interface_method +
                    "&nonce_str=" + NONCE_STR +
                    "&service_id=" + SERVICE_ID +
                    "&sign_type=" + SIGN_TYPE +
                    "&timestamp=" + timestamp +
                    "&version=" + person.version;
            //参数赋值
            JSONObject erpParam = new JSONObject();
            erpParam.put("service_id", SERVICE_ID);
            erpParam.put("app_key", API_KEY);
            erpParam.put("interface_method", person.interface_method);
            erpParam.put("biz_content", person.biz_content);
            erpParam.put("charset", person.charset);
            erpParam.put("nonce_str", NONCE_STR);
            erpParam.put("sign_type", SIGN_TYPE);
            erpParam.put("sign", this.encrypt(API_SECRET,signStr));
            erpParam.put("timestamp", timestamp);
            erpParam.put("version", person.version);





            // 易仓接口请求
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(URL);
            post.addHeader("Content-Type", "application/json");
            System.out.println("接口"+JSONObject.toJSONString(erpParam.getString("interface_method")));
            System.out.println(toPrettyFormat(JSONObject.toJSONString(erpParam)));
            post.setEntity(new StringEntity(JSONObject.toJSONString(erpParam), "UTF-8"));

            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("HTTP请求异常");
                return ResponseEntity.status(HttpStatus.OK).body("HTTP请求异常");
            }
            HttpEntity resEntity = response.getEntity();
            String resEntityData = EntityUtils.toString(resEntity);
            JSONObject result = JSONObject.parseObject(resEntityData);
            if (null == result) {
                System.out.println("HTTP响应体为空");
                return ResponseEntity.status(HttpStatus.OK).body("HTTP响应体为空");
            }
            if (!"200".equals(result.getString("code"))) {
                return ResponseEntity.status(HttpStatus.OK).body(JSONObject.toJSONString(JSONObject.toJSONString(result)));
            }
            JSONObject biz_content = JSONObject.parseObject(result.getString("biz_content"));
            //System.out.println(JSONObject.toJSONString(biz_content));
            //System.out.println(toPrettyFormat(JSONObject.toJSONString(biz_content.getString("data"))));
            return ResponseEntity.status(HttpStatus.OK).body(JSONObject.toJSONString(biz_content));
            //return ResponseEntity.status(HttpStatus.OK).body(this.toPrettyFormat(JSONObject.toJSONString(biz_content.getString("data"))));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("调用异常！");
            return ResponseEntity.status(HttpStatus.OK).body("调用异常！");
        }
    }


        //AES加密
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
                System.out.println("签名异常");
            }
            return Base64.encodeBase64String(result);
        }

        //json格式化
        private String toPrettyFormat(String json) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(jsonObject);
        }

}