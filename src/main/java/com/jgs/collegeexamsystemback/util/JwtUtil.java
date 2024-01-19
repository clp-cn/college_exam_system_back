package com.jgs.collegeexamsystemback.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * @author Administrator
 * @version 1.0
 * @description JwtUtil
 * @date 2023/7/9 0009 16:09
 */
public class JwtUtil {
    // 有效期
    public static final Long JWT_TTL = 7*24*60*60*1000L;
    // 密钥明文
    public static final String JWT_KEY = "clp";

    public static String getUUID(){
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }

    /**
    * @description 生成jwt
    * @param subject
    * @returnType java.lang.String
    * @author Administrator
    * @date  11:40
    */
    public static String createJwt(String subject){
        JwtBuilder builder = getJwtBuilder(subject,null,getUUID()); // 设置过期时间
        return builder.compact();
    }

    /**
    * @description 生成jwt
    * @param subject
     * @param ttlMillis
    * @returnType java.lang.String
    * @author Administrator
    * @date  11:42
    */
    public static String createJwt(String subject,Long ttlMillis){
        JwtBuilder builder = getJwtBuilder(subject,ttlMillis,getUUID()); // 设置过期时间
        return builder.compact();
    }

    public static JwtBuilder getJwtBuilder(String subject,Long ttlMillis,String uuid){
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null){
            ttlMillis = JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .setId(uuid)
                .setSubject(subject)
                .setIssuer("clp")
                .setIssuedAt(now)
                .signWith(signatureAlgorithm,secretKey) // 使用HS256对称加密算法签名，第二个参数为密钥
                .setExpiration(expDate);
    }

    public static String createJwt(String id,String subject,Long ttlMillis){
        JwtBuilder builder = getJwtBuilder(subject,ttlMillis,id);   // 设置过期时间
        return builder.compact();
    }

    /**
    * @description 生成加密后的密钥
    * @returnType javax.crypto.SecretKey
    * @author Administrator
    * @date  11:52
    */
    public static SecretKey generalKey(){
        byte[] encodeKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        SecretKey key = new SecretKeySpec(encodeKey,0,encodeKey.length,"AES");
        return key;
    }

    public static Claims parseJwt(String jwt){
        return Jwts.parser().setSigningKey(generalKey()).parseClaimsJws(jwt).getBody();
    }
}



