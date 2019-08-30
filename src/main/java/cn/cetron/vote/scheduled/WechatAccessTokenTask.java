package cn.cetron.vote.scheduled;

import cn.cetron.vote.utils.WeixinCommenUtil;
import cn.cetron.vote.utils.WeixinConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WechatAccessTokenTask {
    Logger logger = LoggerFactory.getLogger(WechatAccessTokenTask.class);
    @Autowired
    private WeixinCommenUtil weixinCommenUtil;

    // 第一次延迟1秒执行，当执行完后7100秒再执行
    @Scheduled(initialDelay = 1000, fixedDelay = 7100*1000 )//7000*1000
    public void getWeixinAccessToken(){
        try {
            String token = weixinCommenUtil.getToken(WeixinConstants.APPID, WeixinConstants.APPSECRET).getAccess_token();
            logger.info("获取到的微信access_token为"+token);
        } catch (Exception e) {
            logger.error("获取微信access_toke出错，信息如下");
            e.printStackTrace();
            this.getWeixinAccessToken();
        }
    }
}
