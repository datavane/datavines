package io.datavines.server.coordinator.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datavines.server.coordinator.api.dto.vo.SlasVo;
import io.datavines.server.coordinator.repository.entity.Slas;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SlasMapper extends BaseMapper<Slas> {
    List<SlasVo> listSlas(@Param("workSpaceId") Long workSpaceId);
}
