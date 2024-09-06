package com.hyx.git_study;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hyx
 */
@RestController
public class UserController {

    @RequestMapping("/login")
    public String login(){
        return "login success !";
    }



    @RequestMapping("/query")
    public String query(String username){
        return "query success !";
    }

}
