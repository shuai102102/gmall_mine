package com.atguigu.gmall.manage.Controller;

import com.atguigu.gmall.annotation.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("arriListPage")
    public String arriListPage(){
        return "arriListPage";
    }
}
