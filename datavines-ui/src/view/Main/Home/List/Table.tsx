import React from 'react';
import { Table, Popconfirm } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { EyeOutlined, DeleteOutlined } from '@ant-design/icons';
import { IDataSourceListItem, IDataSourceList } from '@/type/dataSource';

type IndexProps = {
    tableData: IDataSourceList,
    onPageChange: any,
    pageParams: { pageNumber: number, pageSize: number },
    goDetail: (record: IDataSourceListItem) => void,
    onEdit: (record: IDataSourceListItem) => void,
    onDelete: (record: IDataSourceListItem) => void,
}

const Index: React.FC<IndexProps> = ({
    tableData, pageParams, onPageChange, goDetail, onEdit, onDelete,
}) => {
    const intl = useIntl();
    const columns: ColumnsType<IDataSourceListItem> = [
        {
            title: intl.formatMessage({ id: 'datasource_modal_name' }),
            dataIndex: 'name',
            key: 'name',
            width: '30%',
            render: (text: string, record: IDataSourceListItem) => <a onClick={() => { goDetail(record); }}>{text}</a>,
        },
        {
            title: intl.formatMessage({ id: 'datasource_modal_source_type' }),
            dataIndex: 'type',
            key: 'type',
            width: '20%',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'datasource_updater' }),
            dataIndex: 'updater',
            key: 'updater',
            width: '20%',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'datasource_updateTime' }),
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: '20%',
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 100,
            render: (text: string, record: IDataSourceListItem) => (
                <>
                    <a onClick={() => { onEdit(record); }}><EyeOutlined /></a>
                    <Popconfirm
                        title={intl.formatMessage({ id: 'common_delete_tip' })}
                        onConfirm={() => { onDelete(record); }}
                        okText={intl.formatMessage({ id: 'common_Ok' })}
                        cancelText={intl.formatMessage({ id: 'common_Cancel' })}
                    >
                        <a style={{ color: '#f81d22', marginLeft: 10 }}><DeleteOutlined /></a>
                    </Popconfirm>
                </>
            ),
        },
    ];
    const onChange = ({ current, pageSize }: any) => {
        onPageChange({
            current,
            pageSize,
        });
    };
    return (
        <Table<IDataSourceListItem>
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
                current: pageParams.pageNumber,
                pageSize: pageParams.pageSize,
            }}
        />
    );
};

export default Index;
