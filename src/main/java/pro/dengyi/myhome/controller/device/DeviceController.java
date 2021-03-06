package pro.dengyi.myhome.controller.device;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.dengyi.myhome.annotations.HolderPermission;
import pro.dengyi.myhome.model.device.Device;
import pro.dengyi.myhome.model.device.dto.DeviceLoginDto;
import pro.dengyi.myhome.model.dto.DeviceDto;
import pro.dengyi.myhome.properties.SystemProperties;
import pro.dengyi.myhome.response.CommonResponse;
import pro.dengyi.myhome.response.DataResponse;
import pro.dengyi.myhome.service.DeviceService;

/**
 * 设备controller
 *
 * @author dengyi (email:dengyi@dengyi.pro)
 * @date 2022-01-23
 */
@Slf4j
@Api(tags = "设备接口")
@Validated
@RestController
@RequestMapping("/device")
public class DeviceController {

  @Autowired
  private DeviceService deviceService;
  @Autowired
  private SystemProperties systemProperties;

  @ApiOperation("分页查询")
  @GetMapping("/page")
  public DataResponse<IPage<DeviceDto>> page(Integer page, Integer size, String floorId,
      String roomId, String productId) {
    IPage<DeviceDto> pageRes = deviceService.page(page, size, floorId, roomId, productId);
    return new DataResponse<>(pageRes);
  }

  @ApiOperation("查询调试所有设备")
  @GetMapping("/debugDeviceList")
  public DataResponse<List<Device>> debugDeviceList(String productId) {
    List<Device> deviceList = deviceService.debugDeviceList(productId);
    return new DataResponse<>(deviceList);
  }

  @HolderPermission
  @ApiOperation("添加或修改设备")
  @PostMapping("/addUpdate")
  public CommonResponse addUpdate(@RequestBody @Validated Device device) {
    deviceService.addUpdate(device);
    return CommonResponse.success();
  }

  @HolderPermission
  @ApiOperation("删除设备")
  @DeleteMapping("/delete/{id}")
  public CommonResponse delete(@PathVariable @NotBlank(message = "id不能为空") String id) {
    deviceService.delete(id);
    return CommonResponse.success();
  }

  @HolderPermission
  @ApiOperation("下发debug命令")
  @PostMapping("/sendDebug")
  public CommonResponse sendDebug(@RequestBody Map<String, Object> orderMap) {
    deviceService.sendDebug(orderMap);
    return CommonResponse.success();
  }


  @ApiOperation("设备登录")
  @PostMapping("/deviceLogin")
  public Map<String, String> deviceLogin(@RequestBody DeviceLoginDto loginDto) {
    log.info("设备登录：{}", loginDto);
    Map<String, String> resMap = new HashMap<>(1);

    //无clientId不进行逻辑
    if (ObjectUtils.isEmpty(loginDto.getClientId())) {
      resMap.put("result", "deny");
    }
    //有clientId进行查找设备是否在系统中
    //1. 服务端
    if (loginDto.getClientId().equals(systemProperties.getServerMqttClientId())) {
      resMap.put("result", "allow");
    } else {
      Device device = deviceService.selectById(loginDto.getClientId());
      if (device == null) {
        resMap.put("result", "deny");
      } else {
        resMap.put("result", "allow");
      }
    }
    return resMap;
  }

  @ApiOperation("EMQ钩子")
  @PostMapping("/emqHook")
  public CommonResponse emqHook(@RequestBody Map<String, Object> params) {
    deviceService.emqHook(params);
    return CommonResponse.success();
  }

}
