package io.datavines.notification.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.notification.core.entity.Slas;
import io.datavines.notification.core.entity.SlasJob;
import io.datavines.notification.core.mapper.SlasMapper;
import io.datavines.notification.core.dto.vo.SlasVo;
import io.datavines.notification.core.service.SlasJobService;
import io.datavines.notification.core.service.SlasService;
import io.datavines.spi.PluginLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SlasServiceImpl extends ServiceImpl<SlasMapper, Slas> implements SlasService {

    @Autowired
    private SlasMapper slasMapper;

    @Autowired
    private SlasJobService slasJobService;

    @Override
    public List<SlasVo> listSlas(Long workSpaceId) {
        List<SlasVo> res = slasMapper.listSlas(workSpaceId);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        boolean removeSlas = removeById(id);
        LambdaQueryWrapper<SlasJob> lambda = new LambdaQueryWrapper<>();
        lambda.eq(SlasJob::getSlasId, id);
        boolean removeSlasJob = slasJobService.remove(lambda);
        boolean result = removeSlas && removeSlasJob;
        return result;
    }

    @Override
    public String getConfigJson(String type) {
        return PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getOrCreatePlugin(type)
                .getConfigJson();
    }
}
