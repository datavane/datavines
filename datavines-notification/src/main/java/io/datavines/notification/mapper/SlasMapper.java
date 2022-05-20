package io.datavines.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datavines.notification.dto.vo.SlasVo;
import io.datavines.notification.entity.Slas;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SlasMapper extends BaseMapper<Slas> {
    List<SlasVo> listSlas(@Param("workSpaceId") Long workSpaceId);
}
