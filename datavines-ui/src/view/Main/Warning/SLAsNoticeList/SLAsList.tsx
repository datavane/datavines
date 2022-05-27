import React, { useState } from 'react';
import { Table, Button } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { TWarnTableData, TWarnTableItem } from '@/type/warning';
import { useCreateSLAs } from './hooks/CreateSLAs';

const Index = () => {
    const intl = useIntl();
    const history = useHistory();
    const match = useRouteMatch();
    const { Render: RenderSLASModal, show } = useCreateSLAs({});
    const [tableData, setTableData] = useState<TWarnTableData>({ list: [{ id: 1, name: 'Dashbord' }], total: 0 });
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
    const onGoMonitor = (record: TWarnTableItem) => {
        console.log(record, match);
        history.push(`${match.path}/SLAs?name=${record.name}&id=${record.id}`);
    };
    const columns: ColumnsType<TWarnTableItem> = [
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
            <RenderSLASModal />
        </div>
    );
};

export default Index;
