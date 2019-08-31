package edu360.bike2bike.bike.Controller;

import edu360.bike2bike.bike.service.Logservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Logcontroller {
    @Autowired
    private Logservice logs;
    @PostMapping("log/ready")
    @ResponseBody
    public String ready(@RequestBody String log){
        logs.save(log);
        return "Ok";
    }
}
