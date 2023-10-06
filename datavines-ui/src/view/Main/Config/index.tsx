import React, { useState } from 'react';
import {
    Table, Button, message, Form,
} from 'antd';
import { ColumnsType } from 'antd/lib/table';
import { useIntl } from 'react-intl';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { PlusOutlined } from '@ant-design/icons';
import {useCreateConfig} from './CreateConfig';
import { useMount, Popconfirm } from '@/common';
import { SearchForm } from '@/component';
import { $http } from '@/http';
import { useSelector } from '@/store';
import Title from "component/Title";
import {TConfigTableItem} from "@/type/config";

const Index = () => {
    const [loading, setLoading] = useState(false);
    const intl = useIntl();
    const form = Form.useForm()[0];
    const history = useHistory();
    const match = useRouteMatch();
    const { Render: RenderSLASModal, show } = useCreateConfig({
        afterClose() {
            getData();
        },
    });
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const [tableData, setTableData] = useState<{ list: TConfigTableItem[], total: number}>({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 10,
    });
    const onPageChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNumber: current,
            pageSize,
        });
        getData({
            pageNumber: current,
            pageSize,
        });
    };
    const getData = async (values: any = null) => {
        try {
            setLoading(true);
            const params = {
                workspaceId,
                ...pageParams,
                ...(values || form.getFieldsValue()),
            };
            const res = (await $http.get('/config/page', params)) || [];
            setTableData({
                list: res?.records || [],
                total: res?.total || 0,
            });
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    const onSearch = (_values: any) => {
        setPageParams({ ...pageParams, pageNumber: 1 });
        getData({
            ..._values,
            pageNumber: 1,
        });
    };
    useMount(() => {
        getData();
    });

    const onEdit = (record: TConfigTableItem) => {
        show(record);
    };
    const onDelete = async (id: number) => {
        try {
            setLoading(true);
            await $http.delete(`/config/${id}`);
            getData();
            message.success(intl.formatMessage({ id: 'common_success' }));
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    const columns: ColumnsType<TConfigTableItem> = [
        {
            title: intl.formatMessage({ id: 'config_var_key' }),
            dataIndex: 'varKey',
            key: 'varKey',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'config_var_value' }),
            dataIndex: 'varValue',
            key: 'varValue',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_updater' }),
            dataIndex: 'updater',
            key: 'updater',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_update_time' }),
            dataIndex: 'updateTime',
            key: 'updateTime',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 150,
            render: (text: string, record: TConfigTableItem) => (
                <>
                    <a onClick={() => { onEdit(record); }}>{intl.formatMessage({ id: 'common_edit' })}</a>
                    <Popconfirm
                        onClick={() => onDelete(record.id)}
                    />
                </>
            ),
        },
    ];
    return (

            <div
                className="dv-page-padding"
                style={
                    {
                        padding: '20px 20px 20px 0px',
                    }
                }
            >
            <Title>
                {intl.formatMessage({ id: 'config_title' })}
            </Title>
            <div className="dv-flex-between" style={{
                marginTop: '20px',
            }}>
                <SearchForm form={form} onSearch={onSearch} placeholder={intl.formatMessage({ id: 'common_search' })} />
                <div style={{ textAlign: 'right', marginBottom: 10 }}>
                    <Button
                        type="primary"
                        onClick={() => { show(null); }}
                        icon={<PlusOutlined />}
                    >
                        {intl.formatMessage({ id: 'create_config' })}
                    </Button>
                </div>
            </div>

            <Table<TConfigTableItem>
                loading={loading}
                size="middle"
                rowKey="id"
                columns={columns}
                dataSource={tableData.list || []}
                onChange={onPageChange}
                bordered
                pagination={{
                    size: 'small',
                    total: tableData.total,
                    showSizeChanger: true,
                    current: pageParams.pageNumber,
                    pageSize: pageParams.pageSize,
                }}
            />
            <RenderSLASModal />
        </div>
    );
};

export default Index;
