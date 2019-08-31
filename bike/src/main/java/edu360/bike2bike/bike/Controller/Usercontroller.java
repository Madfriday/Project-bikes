package edu360.bike2bike.bike.Controller;

import edu360.bike2bike.bike.DataSource.User;
import edu360.bike2bike.bike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.util.List;

@Controller
public class Usercontroller {
    @Autowired
    private UserService userService;

    @RequestMapping("/zz")
    public String index(){
        return "index";
    }

    @PostMapping("/reg")
    @ResponseBody
    public String register(@RequestBody String params) {
        System.out.println(params);
        //userService.register(params);
        return "success";
    }
    @PostMapping("/verify")
    @ResponseBody
    public boolean verify(User user) {
        boolean flag = userService.verify(user);
        return flag;
    }

    @PostMapping("/deposit")
    @ResponseBody
    public String deposit(User user) {
        userService.deposit(user);
        return "success";
    }
    @PostMapping("/identify")
    @ResponseBody
    public String identify(User user) {
        userService.identify(user);
        return "success";
    }

    @GetMapping("/phoneNum/{openid}")
    @ResponseBody
    public User getPhoneNum(@PathVariable("openid") String openid) {
        User user = userService.getUserByOpenid(openid);
        return user;
    }


    @PostMapping("/genCode")
    @ResponseBody
    public String genCode(String nationCode, String phoneNum) {
        String msg = "true";
        try {
            //生成4位随机数 -> 调用短信接口发送验证码 -> 将手机号对应的验证码保存到redis中，并且设置这个key的有效时长
            userService.genVerifyCode(nationCode, phoneNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }



    @PostMapping("/recharge")
    @ResponseBody
    public boolean recharge(@RequestBody String params) {
        boolean flag = true;
        //System.out.println(params);
        userService.recharge(params);
        return flag;
    }


    @RequestMapping("/welcome")
    public String welcome() {
        return "user/welcome";
    }

    //方法上没有加ResponseBoy，那么转发到指定名字的视图上
    @RequestMapping("/user_list")
    public String list() {
        return "user/list";
    }

    @RequestMapping("/user_add")
    public String toAdd() {
        return "user/add";
    }

    @RequestMapping("/user_edit")
    public String toEdit() {
        return "user/edit";
    }

    @PostMapping("/user")
    //通过Ajax方式进行请求，页面不跳转
    @ResponseBody
    public String add(User user) {
        userService.save(user);
        return "success";
    }

    @PostMapping("/user_edit")
    @ResponseBody
    public String edit(User user) {
        userService.update(user);
        return "success";
    }

    @DeleteMapping("/user/{ids}")
    @ResponseBody
    public String deleteByIds(@PathVariable("ids") Long[] ids) {
        userService.deleteByIds(ids);
        return "success";
    }

    @GetMapping("/user/{id}")
    @ResponseBody
    public User getById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }

    @GetMapping("/users")
    @ResponseBody
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/login")
    public String toLogin() {
        return "user/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String login(User user, HttpSession session) {
        User exitUser = userService.login(user);
        if (exitUser != null) {
            session.setAttribute("user", exitUser);
            return "redirect:/";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/user_info")
    @ResponseBody
    public User info(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user;
    }
}
