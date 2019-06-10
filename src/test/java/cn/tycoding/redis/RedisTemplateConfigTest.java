package cn.tycoding.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther TyCoding
 * @date 2018/10/11
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisTemplateConfigTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisForString(){
//        redisTemplate.boundValueOps("1_redis_config_test").set("1_value_test");
        redisTemplate.boundValueOps("2_redis_config_test").set("2_value_test");
    }

    @Test
    public void testRedisForList(){
        List list = new ArrayList();
        list.add(0, "测试");
        redisTemplate.boundHashOps("redis_for_list").put("list", list);
    }

    @Test
    public void testRedis(){
        System.out.println(redisTemplate.boundHashOps("seckill").get(1));
    }

    @Test
    public void testRedis2(){
        System.out.println(redisTemplate.boundHashOps("cargoOrders").get(1));
    }

    @Test
    public void testRedis3(){
        double hashIncDouble = redisTemplate.opsForHash().increment("hashInc","map1",3);
        System.out.println("通过increment(H key, HK hashKey, double delta)方法使变量中的键以值的大小进行自增长:" + hashIncDouble);



        long hashIncLong = redisTemplate.opsForHash().increment("hashInc","map2",6);
        System.out.println("通过increment(H key, HK hashKey, long delta)方法使变量中的键以值的大小进行自增长:" + hashIncLong);
    }


}
