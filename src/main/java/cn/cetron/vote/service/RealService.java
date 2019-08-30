package cn.cetron.vote.service;

import cn.cetron.vote.utils.DesUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 真正的业务代码
 * • 活动时间为3天；
 * • 每个微信账号活动期间只能投1票；
 * • 每个选⼿每分钟获票上限为20票；
 */
@RestController
@Log
public class RealService {
    @Autowired
    private  MongoTemplate mongoTemplate;

    static String collectionName="CetronVote";
    static String statistic="CetronVoteTimesStatistic";
    static String illegalVote="illegal";

    static SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");

    static SimpleDateFormat simpleDateFormat_m=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    static SimpleDateFormat simpleDateFormat_s=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static String startTime="2019-08-20 10:00:00";

    static String endTime="2019-09-30 23:59:59";

    @GetMapping(value = "/validateCetronVote")
    public JSONObject checkVote(HttpServletRequest request){
        String info="";
        JSONObject jsonObject=new JSONObject();
        String openId=request.getParameter("openId");//openId
        String[] optionId=request.getParameterValues("optionId");//多选，checkbox数组
        if(openId==null||openId.equals("")||openId.equals("undefined")){
            log.info("openId为空！");
            info=Arrays.toString(optionId)+"，非法投票！";
            mongoTemplate.save(info,illegalVote);
            jsonObject.put("code",0);
            jsonObject.put("msg","投票失败，请返回主页面重试");
            return jsonObject;
        }
        if(optionId==null||optionId.length<=0){
            log.info("未知投票！");
            info=Arrays.toString(optionId)+"，非法投票！";
            mongoTemplate.save(info,illegalVote);
            jsonObject.put("code",0);
            jsonObject.put("msg","投票失败，请返回主页面重试");
            return jsonObject;
        }
        try {
            openId = DesUtil.deCode(openId);
            if(!openId.contains("_cetron")){
                info=openId+"==>"+Arrays.toString(optionId)+"，非法投票！";
                mongoTemplate.save(info,illegalVote);
                jsonObject.put("code",0);
                jsonObject.put("msg","投票失败，请返回主页面重试");
                return jsonObject;
            }
            openId = openId.split("_cetron")[0];
        } catch (Exception e) {
            info=openId+"==>"+Arrays.toString(optionId)+"，非法投票！";
            log.info(info);
            mongoTemplate.save(info,illegalVote);
            jsonObject.put("code",0);
            jsonObject.put("msg","投票失败，请返回主页面重试");
            return jsonObject;
        }
        System.out.println("新投票：openId："+openId+"，选项："+ Arrays.toString(optionId));

        String check=checkActivityTime();
        if(!check.equals("true")){
            jsonObject.put("code",0);
            jsonObject.put("msg",check);
            return jsonObject;
        }

        //开始业务
        List<JSONObject> myVotes=mongoTemplate.find(new Query(Criteria.where("openId").is(openId)),JSONObject.class,statistic);
        if(myVotes.size()>=3){
            log.info(openId+"投票次数已用完!");
            jsonObject.put("code",1);
            jsonObject.put("msg","对不起，活动期间只能投三票哦");
            List<JSONObject> allVotes=showVotes();
            jsonObject.put("allVotes",allVotes);
            return jsonObject;
        }else {
            //每个选⼿每分钟获票上限为20票,在12:00:00–12:01:00区间内，当选⼿获票达到上限20票时,进行提示
            Date now=new Date();
            String minute_string=simpleDateFormat_m.format(now)+":00";//现在时间的分钟
            long inOneMinute=simpleDateFormat_s.parse(minute_string,new ParsePosition(0)).getTime()-5*60*1000;
            Query queryOptionIdVotes=new Query(Criteria.where("voteOptionId").is(optionId).and("voteTime").gte(inOneMinute));
            List<JSONObject> optionIdVotes=mongoTemplate.find(queryOptionIdVotes,JSONObject.class,collectionName);
            if(optionIdVotes.size()>=20){
                log.info(optionId+"每分钟获票上限为20票！");
                jsonObject.put("code",0);
                jsonObject.put("msg","现在有点忙，请稍等⼀会⼉再投票哦~");
                return jsonObject;
            }
            for(int i=0;i<optionId.length;i++) {
                if ("".equals(optionId[i])) {
                    continue;
                }
                JSONObject myNewVote=new JSONObject();
                myNewVote.put("openId",openId);
                myNewVote.put("voteTime",now.getTime());
                myNewVote.put("voteOptionId",optionId[i]);
                mongoTemplate.save(myNewVote,collectionName);
            }
            JSONObject myVoteTimes=new JSONObject();//记录投票次数
            myVoteTimes.put("openId",openId);
            myVoteTimes.put("voteTime",now.getTime());
            mongoTemplate.save(myVoteTimes,"CetronVoteTimesStatistic");
            log.info(openId+"投票成功");
            List<JSONObject> allVotes=showVotes();
            jsonObject.put("code",1);
            jsonObject.put("msg","投票成功");
            jsonObject.put("allVotes",allVotes);
            return jsonObject;
        }
    }

    @GetMapping("/getVotes")
    public JSONObject getVotes(){
        JSONObject jsonObject=new JSONObject();
        List<JSONObject> allVotes=showVotes();
        jsonObject.put("allVotes",allVotes);
        return jsonObject;
    }


    public  List<JSONObject> showVotes(){
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add( Aggregation.project("voteOptionId"));
        operations.add( Aggregation.group( "voteOptionId" ).count().as( "counts" ).first( "voteOptionId" ).as(
                "voteOptionId" ));
        operations.add(Aggregation.sort(new Sort(new Sort.Order(Sort.Direction.DESC, "counts"))));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<JSONObject> results = mongoTemplate.aggregate(aggregation, collectionName, JSONObject.class);
        List<JSONObject> allVotes =  results.getMappedResults();
        for (int i = 0; i < allVotes.size(); i++) {
            JSONObject jsonObject =  allVotes.get(i);
            jsonObject.put("id",i+1);
        }
        return allVotes;
    }

    public static String checkActivityTime(){
        long now=new Date().getTime();
        long startTime_long=simpleDateFormat_s.parse(startTime,new ParsePosition(0)).getTime();
        long endTime_long=simpleDateFormat_s.parse(endTime,new ParsePosition(0)).getTime();
        if(now<startTime_long){
            return "活动还未开始，敬请期待";
        }else if(now>endTime_long){
            return "活动已结束";
        }
        return "true";
    }
}
