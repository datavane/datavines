import React, { useState } from 'react';
import {
    Table, Button, message,
} from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { TUserItem } from '@/type/User';
import { useAddUser } from './useAddUser';
import { $http } from '@/http';
import { useSelector } from '@/store';
import { useMount, Popconfirm } from '@/common';

const Index = () => {
    const intl = useIntl();
    const [loading, setLoading] = useState(false);
    const { Render: RenderWidgetModal, show } = useAddUser({
        afterClose() {
            getData();
        },
    });
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const [tableData, setTableData] = useState<{list: TUserItem[], total: number}>({ list: [], total: 0 });
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
            const params = {
                workspaceId,
                ...pageParams,
            };
            const res = (await $http.get('/workspace/userPage', params)) || [];
            setTableData({
                list: res?.records || [],
                total: res?.total || 0,
            });
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    useMount(() => {
        getData();
    });
    // const onEdit = (record: TUserItem) => {
    //     show(record);
    // };
    const onDelete = async (id: any) => {
        try {
            setLoading(true);
            const res = await $http.delete('/workspace/removeUser', {
                userId: id,
                workspaceId,
            });
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
    const columns: ColumnsType<TUserItem> = [
        {
            title: intl.formatMessage({ id: 'userName_text' }),
            dataIndex: 'username',
            key: 'username',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'email_text' }),
            dataIndex: 'email',
            key: 'email',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'phone_text' }),
            dataIndex: 'phonne',
            key: 'phonne',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 100,
            render: (text: string, record: TUserItem) => (
                <>
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
                            {intl.formatMessage({ id: 'workspace_user_invite' })}
                        </Button>
                    </div>
                </div>
            </div>
            <Table<TUserItem>
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
            <RenderWidgetModal />
        </div>
    );
};

export default Index;
