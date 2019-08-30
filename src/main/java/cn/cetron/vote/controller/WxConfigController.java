package cn.cetron.vote.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/"})
public class WxConfigController {
    @RequestMapping({"MP_verify_5yyMmj1c5ccteXKK.txt"})
    private String returnConfigFile(){
        //把MP_verify_xxxxxx.txt中的内容返回
        return "5yyMmj1c5ccteXKK";
    }
}
