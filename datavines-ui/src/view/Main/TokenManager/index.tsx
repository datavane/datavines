import React, { useState } from 'react';
import {
    Table, Button, message, Form,
} from 'antd';
import { ColumnsType } from 'antd/lib/table';
import { useIntl } from 'react-intl';
import { PlusOutlined } from '@ant-design/icons';
import { useCreateToken } from './CreateToken';
import { useMount, Popconfirm } from '@/common';
import { $http } from '@/http';
import { useSelector } from '@/store';
import Title from "component/Title";
import { TTokenTableItem } from "@/type/token";

const Index = () => {
    const [loading, setLoading] = useState(false);
    const intl = useIntl();
    const form = Form.useForm()[0];
    const { Render: RenderSLASModal, show } = useCreateToken({
        afterClose() {
            getData();
        },
    });
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const [tableData, setTableData] = useState<{ list: TTokenTableItem[], total: number}>({ list: [], total: 0 });
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
            const res = (await $http.get('/token/page', params)) || [];
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

    const onEdit = (record: TTokenTableItem) => {
        show(record);
    };
    const onDelete = async (id: number) => {
        try {
            setLoading(true);
            await $http.delete(`/token/${id}`);
            getData();
            message.success(intl.formatMessage({ id: 'common_success' }));
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    const columns: ColumnsType<TTokenTableItem> = [
        {
            title: intl.formatMessage({ id: 'token_expire_time' }),
            dataIndex: 'expireTime',
            key: 'expireTime',
            width: 180,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'token_token' }),
            dataIndex: 'token',
            key: 'token',
            width: 300,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_update_time' }),
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 180,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'action',
            dataIndex: 'action',
            width: 150,
            render: (text: string, record: TTokenTableItem) => (
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
                {intl.formatMessage({ id: 'token_title' })}
            </Title>
            <div className="dv-flex-between" style={{
                marginTop: '20px',
            }}>
                <div style={{ textAlign: 'right', marginBottom: 10}}>
                    <Button
                        type="primary"
                        onClick={() => { show(null); }}
                        icon={<PlusOutlined />}
                    >
                        {intl.formatMessage({ id: 'create_token' })}
                    </Button>
                </div>
            </div>

            <Table<TTokenTableItem>
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
