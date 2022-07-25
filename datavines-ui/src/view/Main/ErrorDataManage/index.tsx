import React, { useState } from 'react';
import {
    Table, Button, message,
} from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { TWarnTableData, TWarnTableItem } from '@/type/warning';
import { useAddErrorManage } from './useAddErrorManage';
import { $http } from '@/http';
import { useSelector } from '@/store';
import { useMount, Popconfirm } from '@/common';

const Index = () => {
    const intl = useIntl();
    const [loading, setLoading] = useState(false);
    const { Render: RenderAddErrorModal, show } = useAddErrorManage({
        afterClose() {
            getData();
        },
    });
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const [tableData, setTableData] = useState<TWarnTableData>({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 10,
    });
    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNumber: current,
            pageSize,
        });
    };
    const getData = async () => {
        try {
            setLoading(true);
            const res = (await $http.get(`/errorDataStorage/list/${workspaceId}`)) || [];
            setTableData({
                list: res || [],
                total: (res || []).length,
            });
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    useMount(() => {
        getData();
    });
    const onEdit = (record: TWarnTableItem) => {
        show(record);
    };
    const onDelete = async (id: any) => {
        try {
            setLoading(true);
            const res = await $http.delete(`sla/sender/${id}`);
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
    const columns: ColumnsType<TWarnTableItem> = [
        {
            title: intl.formatMessage({ id: 'error_table_store_name' }),
            dataIndex: 'name',
            key: 'name',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'error_table_store_type' }),
            dataIndex: 'type',
            key: 'type',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_updater' }),
            dataIndex: 'updateBy',
            key: 'updateBy',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'warn_update_time' }),
            dataIndex: 'updateTime',
            key: 'updateTime',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 160,
            render: (text: string, record: TWarnTableItem) => (
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
        <div className="dv-page-paddinng">
            <div style={{ paddingTop: '20px' }}>
                <div className="dv-flex-between">
                    <span />
                    <div style={{ textAlign: 'right', marginBottom: 10 }}>
                        <Button type="primary" onClick={() => { show(null); }}>
                            {intl.formatMessage({ id: 'error_create_btn' })}
                        </Button>
                    </div>
                </div>
            </div>
            <Table<TWarnTableItem>
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
                    current: pageParams.pageNumber,
                    pageSize: pageParams.pageSize,
                }}
            />
            <RenderAddErrorModal />
        </div>
    );
};

export default Index;
