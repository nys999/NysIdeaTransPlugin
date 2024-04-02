package com.nys.plugindemo.utils;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Author: nys
 * @ClassName: JedisBean
 * @Date: 2024/4/2
 **/
public class JedisBean {
   static Jedis jedis;

   static {
      InputStream in = JedisBean.class.getClassLoader().getResourceAsStream("nysTranslationConfig.properties");
//      InputStream in = ClassLoader.getSystemResourceAsStream("nysTranslationConfig.properties");
      Properties properties = new Properties();
      try {
         properties.load(in);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      String host=properties.getProperty("redis.host");
      String port=properties.getProperty("redis.port");
      String auth=properties.getProperty("redis.auth");
      jedis=new Jedis(host, Integer.parseInt(port));
      jedis.auth(auth);
   }

   public JedisBean() throws IOException {


   }

   public static Jedis getJedis(){
      return jedis;
   }
}
