package pro.dengyi.myhome.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import pro.dengyi.myhome.dao.DeviceDao;
import pro.dengyi.myhome.dao.DeviceLogDao;
import pro.dengyi.myhome.exception.BusinessException;
import pro.dengyi.myhome.model.Device;
import pro.dengyi.myhome.model.DeviceLog;
import pro.dengyi.myhome.model.dto.DeviceDto;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author dengyi (email:dengyi@dengyi.pro)
 * @date 2022-01-25
 */
@Service
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MqttClient mqttClient;
    @Autowired
    private DeviceLogDao deviceLogDao;


    @Override
    public Device selectById(String deviceId) {
        return deviceDao.selectById(deviceId);
    }

    @Transactional
    @Override
    public void addUpdate(Device device) {
        //todo 其他什么处理？
        if (ObjectUtils.isEmpty(device.getId())) {
            //新增，默认离线，默认启用
            device.setEnable(true);
            device.setOnline(false);

            deviceDao.insert(device);
        } else {
            //更新
            deviceDao.updateById(device);
        }

    }

    @Transactional
    @Override
    public void delete(String id) {
        //todo 做其他判断
        deviceDao.deleteById(id);
    }

    @Override
    public IPage<DeviceDto> page(Integer pageNumber, Integer pageSize, String floorId, String roomId, String categoryId) {
        IPage<DeviceDto> page = new Page<>(pageNumber == null ? 1 : pageNumber, pageSize == null ? 10 : pageSize);
        return deviceDao.selectCustomPage(page, floorId, roomId, categoryId);
    }

    @Override
    public List<Device> debugDeviceList() {
        return deviceDao.selectList(new LambdaQueryWrapper<>());
    }

    @Transactional
    @Override
    public void sendDebug(Map<String, Object> orderMap) {
        String deviceId = (String) orderMap.get("deviceId");
        //判断设备是否在线
        String onlineStatus = stringRedisTemplate.opsForValue().get("onlineDevice:" + deviceId);
        if (!ObjectUtils.isEmpty(onlineStatus)) {
            String pubTopic = "control/" + deviceId;
            String content = (String) orderMap.get("content");
            //todo 有问题
            JSONObject jsonObject = JSON.parseObject(content);
            MqttMessage message = new MqttMessage(JSON.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            try {
                mqttClient.publish(pubTopic, message);
                DeviceLog deviceLog = new DeviceLog();
                deviceLog.setDeviceId(deviceId);
                deviceLog.setDirection(1);
                deviceLog.setPayload(content);
                deviceLog.setCreateTime(new Date());
                deviceLogDao.insert(deviceLog);
            } catch (MqttException e) {
                throw new BusinessException(18001, "下发debug命令异常");
            }
        }

    }
}
