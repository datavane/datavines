import React, { useState } from 'react';
import { Table, Button } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { TWarnTableData, TWarnTableItem } from '@/type/warning';
import { useCreateWidget } from './hooks/CreateWidget';

const Index = () => {
    const intl = useIntl();
    const { Render: RenderWidgetModal, show } = useCreateWidget({});
    const [tableData, setTableData] = useState<TWarnTableData>({ list: [{ id: 1, name: '123' }], total: 0 });
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
    const columns: ColumnsType<TWarnTableItem> = [
        {
            title: intl.formatMessage({ id: 'warn_widget_name' }),
            dataIndex: 'name',
            key: 'name',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_type' }),
            dataIndex: 'type',
            key: 'type',
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
            dataIndex: 'updateTime',
            key: 'updateTime',
            render: (text: string) => <div>{text}</div>,
        },
    ];
    return (
        <div className="dv-page-paddinng">
            <div style={{ textAlign: 'right', marginBottom: 10 }}>
                <Button type="primary" onClick={() => { show(null); }}>
                    {intl.formatMessage({ id: 'warn_create_widget' })}
                </Button>
            </div>
            <Table<TWarnTableItem>
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
            <RenderWidgetModal />
        </div>
    );
};

export default Index;
