import React, { useState } from 'react';
import { Table, Button, Popconfirm } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { IWorkSpaceListItem } from '@/type/workSpace';

const Index = () => {
    const intl = useIntl();
    const [tableData, setTableData] = useState<{ list: IWorkSpaceListItem[], total: number}>({ list: [], total: 0 });
    // const onChange = ({ current, pageSize }: any) => {
    //     setPageParams({
    //         pageNo: current,
    //         pageSize,
    //     });
    // };
    const onEdit = (record: IWorkSpaceListItem) => {
        console.log(record);
    };
    const onDelete = (record: IWorkSpaceListItem) => {
        console.log(record);
    };
    const onSettings = () => {
    };
    const columns: ColumnsType<IWorkSpaceListItem> = [
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
            render: (text: string, record: IWorkSpaceListItem) => (
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
                <Button type="primary" onClick={() => { onSettings(); }}>
                    {intl.formatMessage({ id: 'common_create_btn' })}
                </Button>
            </div>
            <Table<IWorkSpaceListItem>
                size="middle"
                rowKey="id"
                columns={columns}
                dataSource={tableData.list || []}
                // onChange={onChange}
                pagination={{
                    size: 'small',
                    total: tableData.total,
                    showSizeChanger: true,
                    defaultPageSize: 20,
                    // current: pageParams.pageNo,
                    // pageSize: pageParams.pageSize,
                }}
            />
        </div>
    );
};

export default Index;
