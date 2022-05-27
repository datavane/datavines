import React, { useState } from 'react';
import { Table, Button, Popconfirm } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { TWarnMetricTableData, TWarnMetricTableItem } from '@/type/warning';
import { GoBack } from '@/component';

const Index = () => {
    const intl = useIntl();
    const history = useHistory();
    const match = useRouteMatch();
    const [tableData, setTableData] = useState<TWarnMetricTableData>({ list: [{ id: 1, name: '123' }], total: 0 });
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
    const onEdit = (record: TWarnMetricTableItem) => {
        console.log(record);
    };
    const onDelete = (record: TWarnMetricTableItem) => {
        console.log(record);
    };
    const onSettings = () => {
        history.push('/main/warning/SLAsSetting?name=123&id=234');
    };
    const columns: ColumnsType<TWarnMetricTableItem> = [
        {
            title: intl.formatMessage({ id: 'jobs_task_name' }),
            dataIndex: 'name',
            fixed: 'left',
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
            title: intl.formatMessage({ id: 'common_dataSource' }),
            dataIndex: 'dataSource',
            key: 'dataSource',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 120,
            render: (text: string, record: TWarnMetricTableItem) => (
                <>
                    <a style={{ marginRight: 5 }} onClick={() => { onEdit(record); }}>{intl.formatMessage({ id: 'common_edit' })}</a>
                    <Popconfirm
                        title={intl.formatMessage({ id: 'common_delete_tip' })}
                        onConfirm={() => { onDelete(record); }}
                        okText={intl.formatMessage({ id: 'common_Ok' })}
                        cancelText={intl.formatMessage({ id: 'common_Cancel' })}
                    >
                        <a>{intl.formatMessage({ id: 'common_delete' })}</a>
                    </Popconfirm>

                </>
            ),
        },
    ];
    return (
        <div className="dv-page-paddinng">
            <div className="dv-flex-between" style={{ textAlign: 'right', marginBottom: 10 }}>
                <GoBack />
                <Button type="primary" onClick={() => { onSettings(); }}>
                    {intl.formatMessage({ id: 'common_settings' })}
                </Button>
            </div>
            <Table<TWarnMetricTableItem>
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
        </div>
    );
};

export default Index;
