package cn.cetron.vote.controller;

import cn.cetron.vote.service.RealService;
import cn.cetron.vote.utils.DesUtil;
import cn.cetron.vote.utils.HttpRequestUtil;
import cn.cetron.vote.utils.UserInfoUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.List;

@RestController
public class RedirectController {
    @Autowired
    private UserInfoUtil userInfoUtil;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RealService realService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String WX_APPID = "wx3f3ddefbd4612c43";
    public static final String WX_APPSECRET = "65218bd80b820b929378a3b4bcb2b685";

    static String statistic="CetronVoteTimesStatistic";
    /**
     * 微信网页授权流程:
     * 1. 用户同意授权,获取 code
     * 2. 通过 code 换取网页授权 access_token
     * 3. 使用获取到的 access_token 和 openid 拉取用户信息
     * @param code  用户同意授权后,获取到的code
     * @param state 重定向状态参数
     * @return
     */
    @GetMapping("/vote.do")
    public JSONObject wecahtLogin(@RequestParam(name = "code", required = false) String code, @RequestParam(name = "state",required = false) String state) {
        JSONObject returnInfo=new JSONObject();
        // 1. 用户同意授权,获取code
        logger.info("收到微信重定向跳转.");
        logger.info("用户同意授权,获取code:{} , state:{}", code, state);

        //1.1使用缓存，查看code是否存在，如果存在,重新获取,否则直接用

        // 2. 通过code换取网页授权access_token
        if (code != null || !(code.equals(""))) {

            String APPID = WX_APPID;
            String SECRET = WX_APPSECRET;
            String CODE = code;
            String WebAccessToken = "";
            String openId = "";
            String unionid="";
            String nickName,sex,openid = "";
            String REDIRECT_URI = "http://4q43nm.natappfree.cc/vote.do";
            String SCOPE = "snsapi_base";//snsapi_userinfo,snsapi_userbase

            String getCodeUrl = userInfoUtil.getCode(APPID, REDIRECT_URI, SCOPE);
            logger.info("第一步:用户授权, get Code URL:{}", getCodeUrl);

            // 替换字符串，获得请求access token URL
            String tokenUrl = userInfoUtil.getWebAccess(APPID, SECRET, CODE);
            logger.info("第二步:get Access Token URL:{}", tokenUrl);

            // 通过https方式请求获得web_access_token
            JSONObject jsonObject = HttpRequestUtil.httpsRequest(tokenUrl, "GET", null);
            logger.info("请求到的Access Token:{}", jsonObject.toJSONString());

//            {
//                "access_token":"ACCESS_TOKEN",
//                "expires_in":7200,
//                "refresh_token":"REFRESH_TOKEN",
//                "openid":"OPENID",
//                "scope":"SCOPE",
//                "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
//            }

            if (null != jsonObject) {
                try {

                    WebAccessToken = jsonObject.getString("access_token");

                    openId = jsonObject.getString("openid");//将服务号和订阅号绑定，通过unionid获得用户唯一id
                    logger.info("获取access_token成功!");
                    logger.info("WebAccessToken:{} , openId:{}", WebAccessToken, openId);
                    //***如果SCOPE是user_base,则只能获取openId,到此结束
                    logger.info("用户openId:"+openId);

                    List<JSONObject> myVotes=mongoTemplate.find(new Query(Criteria.where("openId").is(openId)),JSONObject.class,statistic);
                    if(myVotes.size()>=3){
                        returnInfo.put("hadVote",true);
                        List<JSONObject> allVotes=realService.showVotes();
                        returnInfo.put("allVotes",allVotes);
                    }else {
                        returnInfo.put("hadVote",false);
                    }
                    try {
                        returnInfo.put("openId",DesUtil.enCode(openId+"_cetron"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return returnInfo;
                    //***如果SCOPE是user_info，则可以继续获取用户基本信息


                    // 3. 使用获取到的 Access_token 和 openid 拉取用户信息
//                    String userMessageUrl = userInfoUtil.getUserMessage(WebAccessToken, openId);
//                    logger.info("第三步:获取用户信息的URL:{}", userMessageUrl);
//
//                    // 通过https方式请求获得用户信息响应
//                    JSONObject userMessageJsonObject = HttpRequestUtil.httpsRequest(userMessageUrl, "GET", null);
//
//                    unionid=userMessageJsonObject.getString("unionid");
//                    logger.info("用户unionid:", unionid);
//                    {
//                        "openid":" OPENID",
//                        "nickname": NICKNAME,
//                        "sex":"1",
//                        "province":"PROVINCE"
//                        "city":"CITY",
//                        "country":"COUNTRY",
//                        "headimgurl":    "http://wx.qlogo.cn/mmopen/g3MoCfHe/46",
//                        "privilege":[
//                              "PRIVILEGE1"
//                              "PRIVILEGE2"
//                        ],
//                        "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
//                    }

//                    if (userMessageJsonObject != null&&!userMessageJsonObject.containsKey("errcode")) {
//                        try {
//                            //用户昵称
//                            nickName = userMessageJsonObject.getString("nickname");
//                            //用户性别
//                            sex = userMessageJsonObject.getString("sex");
//                            sex = (sex.equals("1")) ? "男" : "女";
//                            //用户唯一标识
//                            openid = userMessageJsonObject.getString("openid");
//
//                            logger.info("用户昵称:{}", nickName);
//                            logger.info("用户性别:{}", sex);
//                            logger.info("OpenId:{}", openid);
//
//                            //调用业务
//                            return indexController.index();
//                            //return realService.checkVote(openid,1);
//                        } catch (JSONException e) {
//                            logger.error("获取用户信息失败");
//                        }
//                    }
                } catch (JSONException e) {
                    logger.error("获取Web Access Token失败");
                }
            }
        }
        return null;
    }
}
