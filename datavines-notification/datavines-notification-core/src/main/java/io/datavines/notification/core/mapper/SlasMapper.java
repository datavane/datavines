package io.datavines.notification.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datavines.notification.core.entity.Slas;
import io.datavines.notification.core.dto.vo.SlasVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SlasMapper extends BaseMapper<Slas> {
    List<SlasVo> listSlas(@Param("workSpaceId") Long workSpaceId);
}
