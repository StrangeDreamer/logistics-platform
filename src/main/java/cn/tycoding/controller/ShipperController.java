package cn.tycoding.controller;

import cn.tycoding.service.lp.ShipperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/shipper")
public class ShipperController {
    @Autowired
    private ShipperService shipperService;


    @ResponseBody
    @RequestMapping()
    public int insertShipper(@RequestParam("id") String id,@RequestParam("isActivated") boolean isActivated) {

        return shipperService.insertShipper(id,isActivated);
    }

}
