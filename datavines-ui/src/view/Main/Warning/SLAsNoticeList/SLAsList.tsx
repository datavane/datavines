import React, { useState } from 'react';
import { Table, Button, message } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { EyeOutlined } from '@ant-design/icons';
import { TWarnSLATableItem } from '@/type/warning';
import { useCreateSLAs } from './hooks/CreateSLAs';
import { useMount, Popconfirm } from '@/common';
import { $http } from '@/http';
import { useSelector } from '@/store';

const Index = () => {
    const [loading, setLoading] = useState(false);
    const intl = useIntl();
    const history = useHistory();
    const match = useRouteMatch();
    const { Render: RenderSLASModal, show } = useCreateSLAs({
        afterClose() {
            getData();
        },
    });
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const [tableData, setTableData] = useState<{ list: TWarnSLATableItem[], total: number}>({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNo: 1,
        pageSize: 10,
    });
    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNo: current,
            pageSize,
        });
    };
    const getData = async () => {
        try {
            setLoading(true);
            const res = (await $http.get(`/sla/list/${workspaceId}`)) || [];
            setTableData({
                list: res || [],
                total: res.length || 0,
            });
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    useMount(() => {
        getData();
    });
    const onGoMonitor = (record: TWarnSLATableItem) => {
        console.log(record, match);
        history.push(`${match.path}/SLAs?name=${record.name}&id=${record.id}`);
    };
    const onEdit = (record: TWarnSLATableItem) => {
        show(record);
    };
    const onDelete = async (id: number) => {
        try {
            setLoading(true);
            const res = await $http.delete(`/sla/${id}`);
            if (res) {
                getData();
                message.success(intl.formatMessage({ id: 'common_success' }));
            } else {
                message.success(intl.formatMessage({ id: 'common_fail' }));
            }
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    const columns: ColumnsType<TWarnSLATableItem> = [
        {
            title: intl.formatMessage({ id: 'warn_SLAs_name' }),
            dataIndex: 'name',
            key: 'name',
            render: (text: string, record) => (
                <a onClick={() => {
                    onGoMonitor(record);
                }}
                >
                    {text}
                </a>
            ),
        },
        {
            title: 'Metric',
            dataIndex: 'metric',
            key: 'metric',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: 'Issue',
            dataIndex: 'issue',
            key: 'issue',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'warn_update_time' }),
            dataIndex: 'founder',
            key: 'founder',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 100,
            render: (text: string, record: TWarnSLATableItem) => (
                <>
                    <a onClick={() => { onEdit(record); }}><EyeOutlined /></a>
                    <Popconfirm
                        onClick={() => onDelete(record.id)}
                    />
                </>
            ),
        },
    ];
    return (
        <div className="dv-page-paddinng">
            <div style={{ textAlign: 'right', marginBottom: 10 }}>
                <Button
                    type="primary"
                    onClick={() => { show(null); }}
                >
                    {intl.formatMessage({ id: 'warn_create_SLAs' })}
                </Button>
            </div>
            <Table<TWarnSLATableItem>
                loading={loading}
                size="middle"
                rowKey="id"
                columns={columns}
                dataSource={tableData.list || []}
                onChange={onChange}
                pagination={{
                    size: 'small',
                    total: tableData.total,
                    showSizeChanger: true,
                    defaultPageSize: 20,
                    current: pageParams.pageNo,
                    pageSize: pageParams.pageSize,
                }}
            />
            <RenderSLASModal />
        </div>
    );
};

export default Index;
