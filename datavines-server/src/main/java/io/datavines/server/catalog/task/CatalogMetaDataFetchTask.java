package io.datavines.server.catalog.task;

import java.sql.SQLException;

public interface CatalogMetaDataFetchTask {

    void execute() throws SQLException;

}
