import React, { useState } from 'react';
import { Form, Button, Radio } from 'antd';
import { IdcardOutlined, TableOutlined } from '@ant-design/icons';
import { useIntl } from 'react-intl';
import { useHistory } from 'react-router-dom';
import { IDataSourceListItem, IDataSourceList } from '@/type/dataSource';
import {
    IF, usePersistFn, useWatch, useLoading,
} from '@/common';
import ContentLayout from '@/component/ContentLayout';
import { Title, SearchForm } from '@/component';
import CardList from './List/Card';
import TableList from './List/Table';
import { useAddDataSource } from './AddDataSource';
import { $http } from '@/http';
import { useSelector, useDatasourceActions } from '@/store';

function App() {
    const history = useHistory();
    const intl = useIntl();
    const setLoading = useLoading();
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const form = Form.useForm()[0];
    const { tableType } = useSelector((r) => r.datasourceReducer);
    const { setDatasourceType } = useDatasourceActions();
    const { show, Render } = useAddDataSource({
        afterClose() {
            getData();
        },
    });
    const [tableData, setTableData] = useState<IDataSourceList>({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 20,
    });
    const getData = usePersistFn(async (values = {}) => {
        try {
            const res = (await $http.get('/datasource/page', {
                workSpaceId: workspaceId,
                ...pageParams,
                ...values,
            })) || {};
            setTableData({
                list: res?.records || [],
                total: res.total,
            });
        } catch (error) {
        }
    });
    useWatch([pageParams, workspaceId], () => {
        getData();
    }, { immediate: true });
    const goDetail = usePersistFn((record: IDataSourceListItem) => {
        history.push(`/main/detail/${record.id}/editor`);
    });
    const onSearch = usePersistFn((values) => {
        getData(values);
    });
    const onPageChange = usePersistFn(({ pageNumber, pageSize }) => {
        setPageParams({
            pageNumber,
            pageSize,
        });
    });
    const onEdit = usePersistFn((record: IDataSourceListItem) => {
        show(record);
    });
    const onDelete = usePersistFn(async (record: IDataSourceListItem) => {
        try {
            setLoading(true);
            await $http.delete(`/datasource/${record.id}`);
            getData();
        } catch (error) {
        } finally {
            setLoading(false);
        }
    });
    return (
        <ContentLayout>
            <div>
                <Title>{intl.formatMessage({ id: 'datasource_list_title' })}</Title>
                <div style={{ padding: '25px' }}>
                    <div className="dv-flex-between">
                        <SearchForm form={form} onSearch={onSearch} />
                        <div>
                            <Button
                                type="primary"
                                style={{ marginRight: 15 }}
                                onClick={() => {
                                    show(null);
                                }}
                            >
                                {intl.formatMessage({ id: 'home_create_datasource' })}

                            </Button>
                            <Radio.Group
                                value={tableType}
                                onChange={(e) => {
                                    setDatasourceType(e.target.value);
                                }}
                            >
                                <Radio.Button value="CARD"><IdcardOutlined /></Radio.Button>
                                <Radio.Button value="TABLE"><TableOutlined /></Radio.Button>
                            </Radio.Group>
                        </div>
                    </div>
                    <IF visible={tableType === 'CARD'}>
                        <CardList
                            onEdit={onEdit}
                            onDelete={onDelete}
                            goDetail={goDetail}
                            tableData={tableData}
                            pageParams={pageParams}
                            onPageChange={onPageChange}
                        />
                    </IF>
                    <IF visible={tableType === 'TABLE'}>
                        <TableList
                            onEdit={onEdit}
                            onDelete={onDelete}
                            goDetail={goDetail}
                            tableData={tableData}
                            pageParams={pageParams}
                            onPageChange={onPageChange}
                        />
                    </IF>
                </div>
                <Render />
            </div>
        </ContentLayout>
    );
}

export default App;
