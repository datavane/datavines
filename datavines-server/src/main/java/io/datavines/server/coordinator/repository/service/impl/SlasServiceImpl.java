package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.server.coordinator.api.dto.vo.SlasVo;
import io.datavines.server.coordinator.repository.entity.Slas;
import io.datavines.server.coordinator.repository.entity.SlasJob;
import io.datavines.server.coordinator.repository.mapper.SlasMapper;
import io.datavines.server.coordinator.repository.service.SlasJobService;
import io.datavines.server.coordinator.repository.service.SlasService;
import io.datavines.spi.PluginLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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
    public String getSenderConfigJson(String type) {
        return PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getOrCreatePlugin(type)
                .getConfigSenderJson();
    }

    @Override
    public String getReceiverConfigJson(String type) {
        return PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getOrCreatePlugin(type)
                .getConfigReceiverJson();
    }

    @Override
    public Set<String> getSupportPlugin(){
        Set<String> supportedPlugins = PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getSupportedPlugins();
        return supportedPlugins;
    }
}
