package pro.dengyi.myhome.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import pro.dengyi.myhome.dao.FloorDao;
import pro.dengyi.myhome.model.Floor;
import pro.dengyi.myhome.model.dto.FloorDto;
import pro.dengyi.myhome.service.FloorService;

import java.util.List;

/**
 * @author dengyi (email:dengyi@dengyi.pro)
 * @date 2022-01-23
 */
@Service
public class FloorServiceImpl implements FloorService {
    @Autowired
    private FloorDao floorDao;

    @Transactional
    @Override
    public void addUpdate(Floor floor) {
        if (ObjectUtils.isEmpty(floor.getId())) {
            floorDao.insert(floor);
        } else {
            floorDao.updateById(floor);
        }

    }

    @Transactional
    @Override
    public void delete(String id) {
        floorDao.deleteById(id);
    }

    @Override
    public List<FloorDto> floorList() {
        return floorDao.selectFloorDto();
    }
}