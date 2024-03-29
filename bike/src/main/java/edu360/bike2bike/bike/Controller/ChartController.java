package edu360.bike2bike.bike.Controller;

import edu360.bike2bike.bike.DataSource.ValueName;
import edu360.bike2bike.bike.DataSource.ZoneVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class ChartController {

    @RequestMapping("/charts/posi")
    public String toChart() {
        return "charts/position";
    }

    @RequestMapping("/charts/getPosiData")
    @ResponseBody
    public ZoneVO getPosiData() {
        ZoneVO vo = new ZoneVO();
        vo.setNames(Arrays.asList("海淀区", "昌平区", "东城区", "西城区"));
        List<ValueName> valueNameList = new ArrayList<ValueName>();
        valueNameList.add(new ValueName(666, "海淀区"));
        valueNameList.add(new ValueName(333, "昌平区"));
        valueNameList.add(new ValueName(567, "东城区"));
        valueNameList.add(new ValueName(456, "西城区"));
        valueNameList.add(new ValueName(456, "新都区"));
        vo.setValueNames(valueNameList);
        return vo;
    }


    @RequestMapping("/charts/map")
    public String toMap() {
        return "charts/map1";
    }

    @RequestMapping("/charts/data")
    public String toData() {
        return "charts/map1";
    }
}
