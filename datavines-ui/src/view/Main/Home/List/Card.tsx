import React from 'react';
import {
    Pagination, Card, Dropdown, Menu, Popconfirm, Row, Col,
} from 'antd';
import { EyeOutlined, DeleteOutlined } from '@ant-design/icons';
import { useIntl } from 'react-intl';
import { IDataSourceListItem, IDataSourceList } from '@/type/dataSource';

type IndexProps = {
    tableData: IDataSourceList,
    onPageChange: any,
    pageParams: { pageNumber: number, pageSize: number }
    goDetail: (record: IDataSourceListItem) => void;
    onEdit: (record: IDataSourceListItem) => void,
    onDelete: (record: IDataSourceListItem) => void,
}

const Index: React.FC<IndexProps> = ({
    tableData, pageParams, onPageChange, goDetail, onEdit, onDelete,
}) => {
    const intl = useIntl();
    const onChange = (current: number, pageSize: number) => {
        onPageChange({
            current,
            pageSize,
        });
    };
    const renderItem = (item: IDataSourceListItem) => (
        <React.Fragment key={item.id}>
            <Col span={6}>
                <Card
                    title={(
                        <a onClick={() => {
                            goDetail(item);
                        }}
                        >
                            {item.name}
                        </a>
                    )}
                    extra={(
                        <div onClick={(e) => (e.stopPropagation())}>
                            <Dropdown
                                overlay={(
                                    <Menu
                                        items={[
                                            {
                                                label: <span onClick={() => (onEdit(item))}>{intl.formatMessage({ id: 'common_edit' })}</span>,
                                                key: 'edit',
                                                icon: <EyeOutlined />,
                                            },
                                            {
                                                label: (
                                                    <Popconfirm
                                                        title={intl.formatMessage({ id: 'common_delete_tip' })}
                                                        onConfirm={() => { onDelete(item); }}
                                                        okText={intl.formatMessage({ id: 'common_Ok' })}
                                                        cancelText={intl.formatMessage({ id: 'common_Cancel' })}
                                                    >
                                                        <a style={{ color: '#f81d22' }}>{intl.formatMessage({ id: 'common_delete' })}</a>
                                                    </Popconfirm>
                                                ),
                                                icon: <DeleteOutlined />,
                                                key: 'delete',
                                            },
                                        ]}
                                    />
                                )}
                                placement="bottomLeft"
                            >
                                <a>{intl.formatMessage({ id: 'common_more' })}</a>
                            </Dropdown>
                        </div>
                    )}
                    style={{ marginBottom: '15px' }}
                >
                    <p>
                        {intl.formatMessage({ id: 'datasource_modal_source_type' })}
                        :
                        {' '}
                        {item.type}
                    </p>
                    <p>
                        {intl.formatMessage({ id: 'datasource_updateTime' })}
                        :
                        {' '}
                        {item.updateTime}
                    </p>
                </Card>
            </Col>
        </React.Fragment>
    );
    return (
        <>
            <div className="dv-card-warp">
                <Row gutter={16}>
                    {
                        (tableData.list || []).map((item) => (renderItem(item)))
                    }
                </Row>
            </div>
            <div style={{ textAlign: 'right' }}>
                <Pagination
                    showSizeChanger
                    onChange={onChange}
                    current={pageParams.pageNumber}
                    pageSize={pageParams.pageSize}
                    total={tableData.total}
                />
            </div>
        </>
    );
};

export default Index;
