package com.atguigu.gmall.util;

import io.jsonwebtoken.*;

import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    public static void main(String[] args){

        // 三个参数：系统key，用户私人信息，盐值
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("userId","2");
        stringStringHashMap.put("nickName","博哥");

        String atguigu0228 = encode("atguigu0228", stringStringHashMap, "127.0.0.1");

        System.out.println(atguigu0228);

        // 解密jwt的token
        Map atguigu02281 = decode("atguigu0228", "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IuWNmuWTpSIsInVzZXJJZCI6IjIifQ.eMes12-Sw74FXZBoGh2SmWyJde3cRDH4yx_B9AY0Zvc", "127.0.0.1");

        System.out.println(atguigu02281.get("userId").toString()+atguigu02281.get("nickName").toString());
    }

    /***
     * jwt加密
     * @param key
     * @param map
     * @param salt
     * @return
     */
    public static String encode(String key, Map map, String salt) {

        if(salt != null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();
        return token;
    }

    /***
     * jwt解密
     * @param key
     * @param token
     * @param salt
     * @return
     * @throws SignatureException
     */
    public static  Map decode(String key,String token,String salt)throws SignatureException {
        if(salt!=null){
            key+=salt;
        }
        Claims map = null;

        try {
            map = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        }catch (SignatureException e){
            return null;
        }


        System.out.println("map.toString() = " + map.toString());

        return map;

    }
}
