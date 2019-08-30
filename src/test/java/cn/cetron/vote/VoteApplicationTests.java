package cn.cetron.vote;

import cn.cetron.vote.utils.UserInfoUtil;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VoteApplicationTests {
    @Test
    public void contextLoads() {

    }
}
