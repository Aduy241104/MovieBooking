package com.example.demo.configuration;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@Getter
@Setter
public class VnpayConfig {

    @Value("${vnpay.url}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${frontend.payment.successUrl}")
    private String frontendSuccessUrl;

    @Value("${frontend.payment.failureUrl}")
    private String frontendFailureUrl;


    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND_PAY = "pay";
    public static final String VNP_CURRCODE = "VND";
    public static final String VNP_LOCALE = "vn";
    public static final String VNP_ORDERTYPE = "other";


    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    //Util for VNPAY
    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames); // Sắp xếp tên trường
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);

            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (sb.length() > 0) { // Thêm dấu & nếu không phải là phần tử đầu tiên được thêm vào sb
                    sb.append('&');
                }
                sb.append(fieldName);
                sb.append('=');
                try {
                    // QUAN TRỌNG: Các giá trị phải được URL encode khi tạo chuỗi để hash (khi xác thực return URL)
                    // vì VNPAY hash các giá trị đã được URL encode trên URL họ gửi về.
                    // Các giá trị trong `fields` (Map<String, String> vnpayParams) đã được server tự động URL DECODE.
                    // Nên ta phải ENCODE lại chúng để khớp với cách VNPAY tạo hash.
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    // Nên log lỗi và có thể throw exception
                    throw new RuntimeException("Error encoding field value for VNPAY hash: " + fieldValue, e);
                }
            }
        }
        System.out.println("VnpayConfig.hashAllFields - String to hash (for verifying return): " + sb.toString());
        return hmacSHA512(secretKey, sb.toString());
    }
}