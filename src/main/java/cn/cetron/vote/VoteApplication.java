package cn.cetron.vote;

import cn.cetron.vote.utils.UserInfoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableScheduling
@SpringBootApplication
public class VoteApplication {
    private static UserInfoUtil userInfoUtil=new UserInfoUtil();

    public static void main(String[] args) {
        String REDIRECT_URI = "http://yrp6be.natappfree.cc";
        String SCOPE = "snsapi_base"; // snsapi_userinfo // snsapi_base
        //appId
        String appId = "wx3f3ddefbd4612c43";
        String getCodeUrl = userInfoUtil.getCode(appId, REDIRECT_URI, SCOPE);
        System.out.println("getCodeUrl:"+getCodeUrl);
        SpringApplication.run(VoteApplication.class, args);
    }

    //配置跨域
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 请求头，可选
        corsConfiguration.addAllowedHeader("*");
        // 允许跨域访问的域名，必填
        corsConfiguration.addAllowedOrigin("*");
        // 支持跨域请求的方法，必填
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    // 使用Filter 处理跨域请求
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //设置跨域请求参数，处理跨域请求
        source.registerCorsConfiguration("/**",buildConfig());
        return  new CorsFilter(source);
    }

}
