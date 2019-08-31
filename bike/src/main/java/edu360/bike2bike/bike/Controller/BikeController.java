package edu360.bike2bike.bike.Controller;

import com.alibaba.fastjson.JSONObject;

import edu360.bike2bike.bike.DataSource.Bike;
import edu360.bike2bike.bike.DataSource.BikeInfornmation;
import edu360.bike2bike.bike.service.Bikeservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class BikeController {
    @Autowired
    private Bikeservice service;


    @GetMapping("/bike")
    @ResponseBody
    public String find(BikeInfornmation bike){
        service.save(bike);
        System.out.println(bike);
        return "welcome to  GrapefruitBike";
    }

    @PostMapping("/bike")
    @ResponseBody
    public String save(@RequestBody String as){
        service.save(as);
        return "success";
    }
    @PostMapping("/bike0")
    @ResponseBody
    public String saves(@RequestBody String as){
        Bike b = JSONObject.parseObject(as,Bike.class);
        service.save(b);
        return "success";
    }


    @PostMapping("/bike1")
    @ResponseBody
    public String save1(@RequestBody String as1){

        service.save1(as1);
        return "success";
    }

    @PostMapping("/bike2")
    @ResponseBody
    public String save2(@RequestBody String as1){

        service.save2(as1);
        return "success";
    }


    @GetMapping("/bikes")
    @ResponseBody
    public GeoResults<Bike> findNear(double longitude, double latitude){
       GeoResults<Bike> list = service.findnear(longitude,latitude);
        return list;
    }

    @GetMapping("/bike_list")
    public String tolist(){
        return "bike/list";

    }

    @GetMapping("/bike/{id}")
    @ResponseBody
    public BikeInfornmation getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/bike/{ids}")
    @ResponseBody
    public String deleteByIds(@PathVariable("ids") Long[] ids) {
        service.deleteByIds(ids);
        return "success";
    }
    @PostMapping("/bike_edit")
    @ResponseBody
    public String edit(BikeInfornmation bike) {
        service.update(bike);
        return "success";
    }
    @RequestMapping("/bike_edit")
    public String toEdit() {
        return "bike/edit";
    }

    @RequestMapping("/bike_add")
    public String toAdd() {
        return "bike/add";
    }
    @RequestMapping("/bike_list")
    public String list() {
        return "bike/list";
    }




    @PostMapping("/warn")
    @ResponseBody
    public String ask(@RequestBody String as1){
        System.out.println(as1);
        return "success";
    }

    @PostMapping("/ride")
    @ResponseBody
    public String ride(@RequestBody String as1){
        System.out.println(as1);
        return "success";
    }
}
