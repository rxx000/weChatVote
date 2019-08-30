package cn.cetron.vote.entry;

import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping(value = "/cetronCloudWechatVote")
@Log
public class EntryServlet  {
    private final String token = "cetronCloudWechatVote";
    @GetMapping(value = "/start")
    public void doToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            System.out.println("开始签名校验");
            String signature = request.getParameter("signature");
            System.out.println("signature:" + signature);
            String timestamp = request.getParameter("timestamp");
            System.out.println("timestamp:" + timestamp);
            String nonce = request.getParameter("nonce");
            System.out.println("nonce:" + nonce);
            String echostr = request.getParameter("echostr");
            System.out.println("echostr:" + echostr);
            ArrayList<String> array = new ArrayList<String>();
            array.add(signature);
            array.add(timestamp);
            array.add(nonce);

            //排序
            String sortString = sort(token, timestamp, nonce);
            //加密
            String mytoken = Decript.SHA1(sortString);
            //校验签名
            if (mytoken != null && mytoken != "" && mytoken.equals(signature)) {
                System.out.println("签名校验通过。");
                response.getWriter().println(echostr); //如果检验成功输出echostr，微信服务器接收到此输出，才会确认检验完成。
            } else {
                System.out.println("签名校验失败。");
            }
        } catch(Exception e) {
            log.info("签名认证参数错误！");
        }
    }



    /**
     * 排序方法
     * @param token
     * @param timestamp
     * @param nonce
     * @return
     */
    public static String sort(String token, String timestamp, String nonce) {
        String[] strArray = { token, timestamp, nonce };
        Arrays.sort(strArray);

        StringBuilder sbuilder = new StringBuilder();
        for (String str : strArray) {
            sbuilder.append(str);
        }

        return sbuilder.toString();
    }
}
