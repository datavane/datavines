package io.datavines.server.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.server.repository.entity.catalog.CatalogSchemaChange;

import java.util.List;

public interface CatalogSchemaChangeService extends IService<CatalogSchemaChange> {

    List<CatalogSchemaChange> getSchemaChangeList(String uuid);
}
