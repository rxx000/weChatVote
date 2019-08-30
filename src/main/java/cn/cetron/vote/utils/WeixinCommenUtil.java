package cn.cetron.vote.utils;

import cn.cetron.vote.entity.AccessToken;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeixinCommenUtil {
    Logger logger = LoggerFactory.getLogger(WeixinCommenUtil.class);

    @Autowired
    private HttpRequestUtil httpRequestUtil;

    // 获取access_token的接口地址（GET） 限2000（次/天）
    private static String url=WeixinConstants.ACCESS_TOKEN_URL;

    public AccessToken getToken(String appid, String appsecret){

        AccessToken token=null;
        //访问微信服务器的地址
        String requestUrl=url.replace("APPID", appid).replace("APPSECRET", appsecret);
        //HttpRequestUtil httpRequestUtil=new HttpRequestUtil();
        //创建一个json对象
        JSONObject json =httpRequestUtil.httpsRequest(requestUrl,"GET",null);
        System.out.println("获取到的json格式的Token为:"+json);
        //判断json是否为空
        if (json!=null){

            try{
                token=new AccessToken();
                //将获取的access_token放入accessToken对象中
                token.setAccess_token(json.getString("access_token"));
                //将获取的expires_in时间放入accessToken对象中
                token.setExpires_in(json.getIntValue("expires_in"));
            }
            catch (Exception e){
                token=null;
                e.printStackTrace();
                System.out.println("系统异常！");
            }
        }else {
            token=null;
            // 获取token失败
            logger.error("获取token失败 errcode:{} errmsg:{}");
        }
        return token;

    }
}
